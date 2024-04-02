package bio.terra.pearl.core.service.notification;

import bio.terra.pearl.core.model.notification.Notification;
import bio.terra.pearl.core.model.notification.Trigger;
import bio.terra.pearl.core.service.rule.EnrolleeContext;

/** notification sender -- e.g. emailer or text messager */
public interface NotificationSender {
    void processNotificationAsync(Notification notification, Trigger config, EnrolleeContext ruleData);

    void processNotification(Notification notification, Trigger config, EnrolleeContext ruleData,
                             NotificationContextInfo notificationContextInfo);

    void sendTestNotification(Trigger config, EnrolleeContext ruleData);

    NotificationContextInfo loadContextInfo(Trigger config);

}
