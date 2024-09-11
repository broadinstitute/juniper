package bio.terra.pearl.core.service;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.audit.DataChange;
import bio.terra.pearl.core.model.audit.ParticipantDataChange;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
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
@Slf4j
public abstract class DataAuditedService<M extends BaseEntity, D extends BaseMutableJdbiDao<M>,
        C extends BaseEntity & DataChange, CS extends ImmutableEntityService<C, ?>> {
    protected D dao;
    protected final Logger logger;
    protected final ObjectMapper objectMapper;
    protected final CS dataChangeService;

    public DataAuditedService(D dao, CS dataChangeService, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.dataChangeService = dataChangeService;
        logger = log;
        this.dao = dao;
    }

    @Transactional
    public M create(M model, DataAuditInfo auditInfo) {
        model = dao.create(model);
        if (auditInfo != null) {
            C changeRecord = makeCreationChangeRecord(model, auditInfo);
            dataChangeService.create(changeRecord);
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
            List<C> changeRecords = modelObjs.stream().map(model -> makeCreationChangeRecord(model, auditInfo)).toList();
            dataChangeService.bulkCreate(changeRecords);
        } else {
            // unaudited creation is allowed for contexts where the auditing is happening on the parent entity, but we log it
            logger.info("raw bulk creation: service: {}, {} entities", this.getClass().getSimpleName(), modelObjs.size());
        }
    }

    @Transactional
    public M update(M obj, DataAuditInfo auditInfo) {
        try {
            M oldRecord = find(obj.getId()).get();
            C changeRecord = makeUpdateChangeRecord(obj, oldRecord, auditInfo);
            dataChangeService.create(changeRecord);
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

    public List<M> findAllPreserveOrder(List<UUID> uuids) { return dao.findAllPreserveOrder(uuids); }

    @Transactional
    public void delete(UUID id, DataAuditInfo auditInfo, Set<CascadeProperty> cascade) {
        if (auditInfo != null) {
            M oldRecord = find(id).get();
            C changeRecord = makeDeletionChangeRecord(oldRecord, auditInfo);
            dataChangeService.create(changeRecord);
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
            List<C> records = objs.stream().map(obj -> makeDeletionChangeRecord(obj, auditInfo)).toList();
            dataChangeService.bulkCreate(records);
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

    /**
     * Override this function if you need to do any processing
     * (e.g., pulling in linked objects) before saving an
     * audit change.
     */
    protected M processModelBeforeAuditing(M model) {
        return model;
    }

    protected abstract C makeUpdateChangeRecord(M newModel, M oldModel, DataAuditInfo auditInfo);

    /** note this should be called AFTER the model has been saved, so the generated ID can be included */
    protected abstract C makeCreationChangeRecord(M newModel, DataAuditInfo auditInfo);

    protected abstract C makeDeletionChangeRecord(M oldModel, DataAuditInfo auditInfo);

}
