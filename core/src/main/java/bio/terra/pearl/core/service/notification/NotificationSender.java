package bio.terra.pearl.core.service.notification;

import bio.terra.pearl.core.model.notification.Notification;
import bio.terra.pearl.core.model.notification.TriggeredAction;
import bio.terra.pearl.core.service.rule.EnrolleeRuleData;

/** notification sender -- e.g. emailer or text messager */
public interface NotificationSender {
    void processNotificationAsync(Notification notification, TriggeredAction config, EnrolleeRuleData ruleData);

    void processNotification(Notification notification, TriggeredAction config, EnrolleeRuleData ruleData,
                             NotificationContextInfo notificationContextInfo);

    void sendTestNotification(TriggeredAction config, EnrolleeRuleData ruleData);

    NotificationContextInfo loadContextInfo(TriggeredAction config);

}
