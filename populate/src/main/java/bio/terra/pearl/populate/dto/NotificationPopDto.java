package bio.terra.pearl.populate.dto;

import bio.terra.pearl.core.model.notification.Notification;
import bio.terra.pearl.core.model.notification.NotificationEventType;
import bio.terra.pearl.core.model.notification.NotificationType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class NotificationPopDto extends Notification {
    private NotificationType notificationConfigType;
    private NotificationEventType notificationConfigEventType;
}
