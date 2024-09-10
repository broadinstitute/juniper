package bio.terra.pearl.core.service;

import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.audit.DataChange;

public interface ChangeRecordService<T extends DataChange> {
    T buildCreationRecord(DataAuditInfo auditInfo);

    T buildUpdateRecord(DataAuditInfo auditInfo);
}
