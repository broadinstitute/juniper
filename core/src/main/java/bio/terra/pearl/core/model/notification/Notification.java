package bio.terra.pearl.core.model.notification;

import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.study.StudyEnvAttached;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** record of a notification sending attempt. */
@Getter @Setter @SuperBuilder @NoArgsConstructor
public class Notification extends BaseEntity implements StudyEnvAttached {
    private UUID enrolleeId;
    private Enrollee enrollee;
    private UUID participantUserId;
    private UUID portalEnvironmentId;
    private UUID studyEnvironmentId;
    private UUID triggerId;
    private NotificationDeliveryStatus deliveryStatus;
    private NotificationDeliveryType deliveryType;
    @Builder.Default
    private NotificationType notificationType = NotificationType.PARTICIPANT;
    private String sentTo;
    private String customMessages;
    private String sendgridApiRequestId;
    private SendgridEvent eventDetails;
    @Builder.Default
    private Map<String, String> customMessagesMap = new HashMap<>();
    @Builder.Default
    private int retries = 0;
}
