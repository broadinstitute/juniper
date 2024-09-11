package bio.terra.pearl.core.model.admin;

import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.audit.DataChange;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

/**
 * records a change to admin user data, such as a change to their roles or permissions.
 * Admin equivalent of ParticipantDataChange
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class AdminDataChange extends BaseEntity implements DataChange {
    private UUID responsibleAdminUserId; // id of the user making the change, if it was an admin
    private String systemProcess; // if the change was the result of an automatic process, store Class+method here
    private UUID operationId; // unique id to group operations
    private UUID adminUserId; // id of the admin user whose data was changed
    private UUID portalId; // id of the associated portal
    private UUID modelId; // id of the object corresponding to the audit record
    private String modelName; // either a class (like Profile) or a stableId of a survey
    private String oldValue;
    private String newValue;

    public static AdminDataChange.AdminDataChangeBuilder fromAuditInfo(DataAuditInfo auditInfo) {
        return AdminDataChange.builder()
                .responsibleAdminUserId(auditInfo.getResponsibleAdminUserId())
                .operationId(auditInfo.getOperationId());
    }
}
