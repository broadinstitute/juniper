package bio.terra.pearl.core.service.notification;

import bio.terra.pearl.core.model.notification.NotificationConfig;
import bio.terra.pearl.core.service.rule.EnrolleeRuleData;

public interface NotificationSender {
    void sendNotificationAsync(NotificationConfig config, EnrolleeRuleData ruleData);
    void sendNotification(NotificationConfig config, EnrolleeRuleData ruleData);
}
