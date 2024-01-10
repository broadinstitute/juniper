package bio.terra.pearl.populate.dto.notifications;

import bio.terra.pearl.core.model.notification.Notification;
import bio.terra.pearl.core.model.notification.TriggerEventType;
import bio.terra.pearl.core.model.notification.TriggerType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class NotificationPopDto extends Notification {
    private TriggerType triggerType;
    private TriggerEventType triggerEventType;
}
