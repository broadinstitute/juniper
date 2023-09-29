package bio.terra.pearl.core.service;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.workflow.DataAuditInfo;
import bio.terra.pearl.core.model.workflow.DataChangeRecord;
import bio.terra.pearl.core.service.workflow.DataChangeRecordService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * base class for entities that should have changes tracked via DataChangeRecords.
 * This currently allows 'auditInfo' to be null--a later PR should remove that, once we upgrade all the calling services
 * to also support passing auditInfo to create/deletes
 * */
public abstract class DataAuditedService<M extends BaseEntity, D extends BaseMutableJdbiDao<M>> {
    protected D dao;
    protected final Logger logger;
    protected final ObjectMapper objectMapper;
    protected final DataChangeRecordService dataChangeRecordService;

    public DataAuditedService(D dao, DataChangeRecordService dataChangeRecordService, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.dataChangeRecordService = dataChangeRecordService;
        logger = LoggerFactory.getLogger(this.getClass());
        this.dao = dao;
    }

    @Transactional
    public M create(M model, DataAuditInfo auditInfo) {
        model = dao.create(model);
        if (auditInfo != null) {
            DataChangeRecord changeRecord = makeCreationChangeRecord(model, auditInfo);
            dataChangeRecordService.create(changeRecord);
        } else {
            // unaudited creation is allowed for contexts where the auditing is happening on the parent entity, but we log it
            logger.info("raw creation: model: {}, id: {}", model.getClass().getSimpleName(), model.getId());
        }
        return model;
    }

    @Transactional
    public void bulkCreate(List<M> modelObjs, DataAuditInfo auditInfo) {
        dao.bulkCreate(modelObjs);
        if (auditInfo != null) {
            List<DataChangeRecord> changeRecords = modelObjs.stream().map(model -> makeCreationChangeRecord(model, auditInfo)).toList();
            dataChangeRecordService.bulkCreate(changeRecords);
        } else {
            // unaudited creation is allowed for contexts where the auditing is happening on the parent entity, but we log it
            logger.info("raw bulk creation: service: {}, {} entities", this.getClass().getSimpleName(), modelObjs.size());
        }
    }

    @Transactional
    public M update(M obj, DataAuditInfo auditInfo) {
        try {
            M oldRecord = find(obj.getId()).get();
            DataChangeRecord changeRecord = DataChangeRecord.fromAuditInfo(auditInfo)
                    .modelName(oldRecord.getClass().getSimpleName())
                    .newValue(objectMapper.writeValueAsString(obj))
                    .oldValue(objectMapper.writeValueAsString(oldRecord))
                    .build();
            dataChangeRecordService.create(changeRecord);
        } catch (Exception e) {
            throw new RuntimeException("Could not serialize for audit log", e);
        }
        return dao.update(obj);
    }

    public Optional<M> find(UUID id) {
        return dao.find(id);
    }

    public int count() { return dao.count(); }

    public List<M> findAll() { return dao.findAll(); }
    public List<M> findAll(List<UUID> uuids) { return dao.findAll(uuids); }

    @Transactional
    public void delete(UUID id, DataAuditInfo auditInfo, Set<CascadeProperty> cascade) {
        if (auditInfo != null) {
            M oldRecord = find(id).get();
            DataChangeRecord changeRecord = makeDeletionChangeRecord(oldRecord, auditInfo);
            dataChangeRecordService.create(changeRecord);
        } else {
            // unaudited deletion is allowed for contexts where the auditing is happening on the parent entity, but we log it
            logger.info("raw deletion: service: {}, id: {}", this.getClass().getSimpleName(), id);
        }
        dao.delete(id);
    }

    @Transactional
    public void  delete(UUID id, DataAuditInfo auditInfo) {
        delete(id, auditInfo, CascadeProperty.EMPTY_SET);
    }

    @Transactional
    public void bulkDeleteByIds(List<UUID> ids, DataAuditInfo auditInfo) {
        List<M> objs = findAll(ids);
        bulkDelete(objs, auditInfo);
    }

    @Transactional
    public void bulkDelete(List<M> objs, DataAuditInfo auditInfo) {
        if (auditInfo != null) {
            List<DataChangeRecord> records = objs.stream().map(obj -> makeDeletionChangeRecord(obj, auditInfo)).toList();
            dataChangeRecordService.bulkCreate(records);
        } else {
            // unaudited deletion is allowed for contexts where the auditing is happening on the parent entity, but we log it
            logger.info("raw bulk deletion: service: {}, {} objects", this.getClass().getSimpleName(), objs.size());
        }
        dao.deleteAll(objs.stream().map(obj -> obj.getId()).toList());
    }

    /**
     * clears the id field, and resets the createdAt, and lastUpdatedAt fields of the object.
     * returns itself for easy chaining
     */
    public M cleanForCopying(M obj) {
        obj.setLastUpdatedAt(Instant.now());
        obj.setCreatedAt(Instant.now());
        obj.setId(null);
        return obj;
    }

    protected DataChangeRecord makeCreationChangeRecord(M newModel, DataAuditInfo auditInfo) {
        try {
            DataChangeRecord changeRecord = DataChangeRecord.fromAuditInfo(auditInfo)
                    .modelName(newModel.getClass().getSimpleName())
                    .newValue(objectMapper.writeValueAsString(newModel))
                    .oldValue(null)
                    .build();
            return changeRecord;
        } catch (Exception e) {
            throw new RuntimeException("Could not serialize for audit log", e);
        }
    }

    protected DataChangeRecord makeDeletionChangeRecord(M oldModel, DataAuditInfo auditInfo) {
        try {
            DataChangeRecord changeRecord = DataChangeRecord.fromAuditInfo(auditInfo)
                    .modelName(oldModel.getClass().getSimpleName())
                    .newValue(null)
                    .oldValue(objectMapper.writeValueAsString(oldModel))
                    .build();
            return changeRecord;
        } catch (Exception e) {
            throw new RuntimeException("Could not serialize for audit log", e);
        }
    }

}
