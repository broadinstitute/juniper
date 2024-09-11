package bio.terra.pearl.core.service.admin;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.admin.AdminDataChange;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.service.DataAuditedService;
import com.fasterxml.jackson.databind.ObjectMapper;

    public abstract class AdminDataAuditedService<M extends BaseEntity, D extends BaseMutableJdbiDao<M>>
        extends DataAuditedService<M, D, AdminDataChange, AdminDataChangeService> {

    public AdminDataAuditedService(D dao, AdminDataChangeService dataChangeService, ObjectMapper objectMapper) {
        super(dao, dataChangeService, objectMapper);
    }

    /** the overridden methods below share a lot with the ParticipantDataAuditedService, but we can't use inheritance
     * because the change records are different types. */
    @Override
    protected AdminDataChange makeUpdateChangeRecord(M newRecord, M oldRecord, DataAuditInfo auditInfo) {
        try {
            AdminDataChange changeRecord = AdminDataChange.fromAuditInfo(auditInfo)
                    .adminUserId(auditInfo.getAdminUserId())
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

    @Override
    protected AdminDataChange makeCreationChangeRecord(M newModel, DataAuditInfo auditInfo) {
        try {
            AdminDataChange changeRecord = AdminDataChange.fromAuditInfo(auditInfo)
                    .modelName(newModel.getClass().getSimpleName())
                    .modelId(newModel.getId())
                    .adminUserId(auditInfo.getAdminUserId())
                    .newValue(objectMapper.writeValueAsString(processModelBeforeAuditing(newModel)))
                    .oldValue(null)
                    .build();
            return changeRecord;
        } catch (Exception e) {
            throw new RuntimeException("Could not serialize for audit log", e);
        }
    }

    @Override
    protected AdminDataChange makeDeletionChangeRecord(M oldModel, DataAuditInfo auditInfo) {
        try {
            AdminDataChange changeRecord = AdminDataChange.fromAuditInfo(auditInfo)
                    .adminUserId(auditInfo.getAdminUserId())
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
