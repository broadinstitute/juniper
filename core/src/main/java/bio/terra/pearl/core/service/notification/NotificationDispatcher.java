package bio.terra.pearl.core.service.notification;

import bio.terra.pearl.core.model.notification.*;
import bio.terra.pearl.core.service.rule.EnrolleeRuleData;
import bio.terra.pearl.core.service.rule.RuleEvaluator;
import bio.terra.pearl.core.service.workflow.DispatcherOrder;
import bio.terra.pearl.core.service.workflow.EnrolleeEvent;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
public class NotificationDispatcher {
    private static final Logger logger = LoggerFactory.getLogger(NotificationDispatcher.class);
    private NotificationConfigService notificationConfigService;
    private NotificationService notificationService;
    private Map<NotificationDeliveryType, NotificationSender> senderMap;

    public NotificationDispatcher(NotificationConfigService notificationConfigService,
                                  NotificationService notificationService, EmailService emailService) {
        this.notificationConfigService = notificationConfigService;
        this.notificationService = notificationService;
        senderMap = Map.of(NotificationDeliveryType.EMAIL, emailService);
    }

    /** notifications could be triggered by just about anything, so listen to all enrollee events */
    @EventListener
    @Order(DispatcherOrder.NOTIFICATION)
    public void handleEvent(EnrolleeEvent event) {
        List<NotificationConfig> configs = notificationConfigService
                .findByStudyEnvironmentId(event.getEnrollee().getStudyEnvironmentId(), true)
                .stream().filter(config  -> config.getNotificationType().equals(NotificationType.EVENT))
                .toList();
        for (NotificationConfig config: configs) {
            Class configClass = config.getEventType().eventClass;
            if (configClass.isInstance(event)) {
                if (RuleEvaluator.evaluateEnrolleeRule(config.getRule(), event.getEnrolleeRuleData())) {
                    dispatchNotificationAsync(config, event.getEnrolleeRuleData(),
                            event.getPortalParticipantUser().getPortalEnvironmentId());
                }
            }
        }
    }

    protected void dispatchNotificationAsync(NotificationConfig config, EnrolleeRuleData enrolleeRuleData, UUID portalEnvId) {
        Notification notification = initializeNotification(config, enrolleeRuleData, portalEnvId);
        notification = notificationService.create(notification);
        senderMap.get(config.getDeliveryType())
                .processNotificationAsync(notification, config, enrolleeRuleData);
    }

    public void dispatchNotification(NotificationConfig config, EnrolleeRuleData enrolleeRuleData,
                                     NotificationContextInfo notificationContextInfo) {
        Notification notification = initializeNotification(config, enrolleeRuleData,
                notificationContextInfo.portalEnv().getId());
        senderMap.get(config.getDeliveryType())
                .processNotification(notification, config, enrolleeRuleData, notificationContextInfo);
    }

    public void dispatchTestNotification(NotificationConfig config, EnrolleeRuleData enrolleeRuleData) throws Exception {
        senderMap.get(config.getDeliveryType())
                .sendTestNotification(config, enrolleeRuleData);
    }

    public Notification initializeNotification(NotificationConfig config, EnrolleeRuleData ruleData, UUID portalEnvId) {
        return Notification.builder()
                .enrolleeId(ruleData.enrollee().getId())
                .participantUserId(ruleData.enrollee().getParticipantUserId())
                .notificationConfigId(config.getId())
                .deliveryStatus(NotificationDeliveryStatus.READY)
                .deliveryType(config.getDeliveryType())
                .studyEnvironmentId(ruleData.enrollee().getStudyEnvironmentId())
                .portalEnvironmentId(portalEnvId)
                .retries(0)
                .build();
    }

    public NotificationContextInfo loadContextInfo(NotificationConfig config) {
        return senderMap.get(config.getDeliveryType()).loadContextInfo(config);
    }


}
