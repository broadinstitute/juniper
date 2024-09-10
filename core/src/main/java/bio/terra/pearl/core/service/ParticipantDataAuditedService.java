package bio.terra.pearl.core.service;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.audit.ParticipantDataChange;
import bio.terra.pearl.core.service.workflow.ParticipantDataChangeService;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class ParticipantDataAuditedService<M extends BaseEntity, D extends BaseMutableJdbiDao<M>>
        extends DataAuditedService<M, D, ParticipantDataChange, ParticipantDataChangeService> {

    public ParticipantDataAuditedService(D dao, ParticipantDataChangeService dataChangeService, ObjectMapper objectMapper) {
        super(dao, dataChangeService, objectMapper);
    }

    protected ParticipantDataChange makeUpdateChangeRecord(M newRecord, M oldRecord, DataAuditInfo auditInfo) {
        try {
            ParticipantDataChange changeRecord = ParticipantDataChange.fromAuditInfo(auditInfo)
                    .modelName(oldRecord.getClass().getSimpleName())
                    .modelId(newRecord.getId())
                    .newValue(objectMapper.writeValueAsString(processModelBeforeAuditing(newRecord)))
                    .oldValue(objectMapper.writeValueAsString(processModelBeforeAuditing(oldRecord)))
                    .build();
            return changeRecord;
        } catch (Exception e) {
            throw new RuntimeException("Could not serialize for audit log", e);
        }
    }

    /** note this should be called AFTER the model has been saved, so the generated ID can be included */
    protected ParticipantDataChange makeCreationChangeRecord(M newModel, DataAuditInfo auditInfo) {
        try {
            ParticipantDataChange changeRecord = ParticipantDataChange.fromAuditInfo(auditInfo)
                    .modelName(newModel.getClass().getSimpleName())
                    .modelId(newModel.getId())
                    .newValue(objectMapper.writeValueAsString(processModelBeforeAuditing(newModel)))
                    .oldValue(null)
                    .build();
            return changeRecord;
        } catch (Exception e) {
            throw new RuntimeException("Could not serialize for audit log", e);
        }
    }

    protected ParticipantDataChange makeDeletionChangeRecord(M oldModel, DataAuditInfo auditInfo) {
        try {
            ParticipantDataChange changeRecord = ParticipantDataChange.fromAuditInfo(auditInfo)
                    .modelName(oldModel.getClass().getSimpleName())
                    .modelId(oldModel.getId())
                    .newValue(null)
                    .oldValue(objectMapper.writeValueAsString(processModelBeforeAuditing(oldModel)))
                    .build();
            return changeRecord;
        } catch (Exception e) {
            throw new RuntimeException("Could not serialize for audit log", e);
        }
    }
}
