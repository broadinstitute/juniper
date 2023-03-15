package bio.terra.pearl.core.service.notification;

import bio.terra.pearl.core.model.notification.Notification;
import bio.terra.pearl.core.model.notification.NotificationConfig;
import bio.terra.pearl.core.service.rule.EnrolleeRuleData;

/** notification sender -- e.g. emailer or text messager */
public interface NotificationSender {
    void processNotificationAsync(Notification notification, NotificationConfig config, EnrolleeRuleData ruleData);

    void processNotification(Notification notification, NotificationConfig config, EnrolleeRuleData ruleData,
                             NotificationContextInfo notificationContextInfo);

    void sendTestNotification(NotificationConfig config, EnrolleeRuleData ruleData) throws Exception;

    NotificationContextInfo loadContextInfo(NotificationConfig config);

}
