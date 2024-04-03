package bio.terra.pearl.core.model.notification;

import bio.terra.pearl.core.model.BaseEntity;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/** record of a notification sending attempt. */
@Getter @Setter @SuperBuilder @NoArgsConstructor
public class Notification extends BaseEntity {
    private UUID enrolleeId;
    private UUID participantUserId;
    private UUID portalEnvironmentId;
    private UUID studyEnvironmentId;
    private UUID triggerId;
    private NotificationDeliveryStatus deliveryStatus;
    private NotificationDeliveryType deliveryType;
    private String sentTo;
    private String customMessages;
    private String sendgridApiRequestId;
    private SendgridEvent eventDetails;
    @Builder.Default
    private Map<String, String> customMessagesMap = new HashMap<>();
    @Builder.Default
    private int retries = 0;
}
