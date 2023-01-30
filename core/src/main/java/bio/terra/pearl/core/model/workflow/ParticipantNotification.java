package bio.terra.pearl.core.model.workflow;

import bio.terra.pearl.core.model.BaseEntity;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Builder
public class ParticipantNotification extends BaseEntity {
    private Instant sentAt;
    private String status;
    private UUID participantTaskId;
    private UUID portalParticipantUserId;
    private NotificationType notificationType;
}
