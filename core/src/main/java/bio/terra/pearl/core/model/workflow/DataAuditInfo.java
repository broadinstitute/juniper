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
    private UUID enrolleeId;
    private UUID portalParticipantUserId;
    private UUID surveyId;
    private UUID operationId;

    public static DataAuditInfo fromAdminUserId(UUID responsibleAdminUserId) {
        return DataAuditInfo.builder()
                .responsibleAdminUserId(responsibleAdminUserId)
                .operationId(UUID.randomUUID())
                .build();
    }

    public static DataAuditInfo fromUserId(UUID userId) {
        return DataAuditInfo
                .builder()
                .responsibleUserId(userId)
                .operationId(UUID.randomUUID())
                .build();
    }

    public static DataAuditInfo fromPortalParticipantUserId(UUID portalParticipantUserId, UUID responsibleUserId) {
        return DataAuditInfo.builder()
                .responsibleUserId(responsibleUserId)
                .portalParticipantUserId(portalParticipantUserId)
                .operationId(UUID.randomUUID())
                .build();
    }

    public static DataAuditInfo fromEnrolleeId(UUID enrolleeId, UUID portalParticipantUserId, UUID responsibleUserId) {
        return DataAuditInfo
                .builder()
                .enrolleeId(enrolleeId)
                .responsibleUserId(responsibleUserId)
                .portalParticipantUserId(portalParticipantUserId)
                .operationId(UUID.randomUUID())
                .build();
    }
}