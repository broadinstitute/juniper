package bio.terra.pearl.core.model.workflow;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

/** subset of DataChangeRecord fields useful for passing between methods that handle grouped changes */
@Getter
@Setter
@SuperBuilder
public class DataAuditInfo {
    private UUID responsibleUserId;
    private UUID responsibleAdminUserId;
    private UUID operationId;

    public static DataAuditInfo fromAdminUserId(UUID responsibleAdminUserId) {
        return DataAuditInfo.builder()
                .responsibleAdminUserId(responsibleAdminUserId)
                .operationId(UUID.randomUUID())
                .build();
    }
}