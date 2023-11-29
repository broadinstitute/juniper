package bio.terra.pearl.core.service.notification;

import bio.terra.pearl.core.model.notification.*;
import bio.terra.pearl.core.service.notification.email.EnrolleeEmailService;
import bio.terra.pearl.core.service.rule.EnrolleeRuleData;
import bio.terra.pearl.core.service.rule.RuleEvaluator;
import bio.terra.pearl.core.service.workflow.DispatcherOrder;
import bio.terra.pearl.core.service.workflow.EnrolleeEvent;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NotificationDispatcher {
    private NotificationConfigService notificationConfigService;
    private NotificationService notificationService;
    private Map<NotificationDeliveryType, NotificationSender> senderMap;

    public NotificationDispatcher(NotificationConfigService notificationConfigService,
                                  NotificationService notificationService, EnrolleeEmailService enrolleeEmailService) {
        this.notificationConfigService = notificationConfigService;
        this.notificationService = notificationService;
        senderMap = Map.of(NotificationDeliveryType.EMAIL, enrolleeEmailService);
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

    /**
     * if we invoke the EmailService async, then we save the notification first so that we have a record of it.
     * If we invoke it synchronously, then we don't save it. Because if it's a synchronous call and the process gets killed,
     * the surrounding transaction will roll back (e.g. undoing the enrollee creation) and so we don't want a saved notification.
     * Where this will help is for bulk operations -- if we want to send out 2000 emails to all the ourHealth participants
     * because of a new survey, it lets us have just 1 database operation per notification instead of 2
     * */
    protected void dispatchNotificationAsync(NotificationConfig config, EnrolleeRuleData enrolleeRuleData, UUID portalEnvId) {
        Notification notification = initializeNotification(config, enrolleeRuleData, portalEnvId, null);
        notification = notificationService.create(notification);
        senderMap.get(config.getDeliveryType())
                .processNotificationAsync(notification, config, enrolleeRuleData);
    }

    public void dispatchNotification(NotificationConfig config, EnrolleeRuleData enrolleeRuleData,
                                     NotificationContextInfo notificationContextInfo) {
        dispatchNotification(config, enrolleeRuleData, notificationContextInfo, Map.of());
    }

    public void dispatchNotification(NotificationConfig config, EnrolleeRuleData enrolleeRuleData,
                                     NotificationContextInfo notificationContextInfo, Map<String, String> customMessages) {
        Notification notification = initializeNotification(config, enrolleeRuleData,
            notificationContextInfo.portalEnv().getId(), customMessages);
        senderMap.get(config.getDeliveryType())
            .processNotification(notification, config, enrolleeRuleData, notificationContextInfo);
    }

    public void dispatchTestNotification(NotificationConfig config, EnrolleeRuleData enrolleeRuleData) throws Exception {
        senderMap.get(config.getDeliveryType())
                .sendTestNotification(config, enrolleeRuleData);
    }

    public Notification initializeNotification(NotificationConfig config, EnrolleeRuleData ruleData,
                                               UUID portalEnvId, Map<String, String> customMessages) {
        return Notification.builder()
                .enrolleeId(ruleData.enrollee().getId())
                .participantUserId(ruleData.enrollee().getParticipantUserId())
                .notificationConfigId(config.getId())
                .deliveryStatus(NotificationDeliveryStatus.READY)
                .deliveryType(config.getDeliveryType())
                .studyEnvironmentId(ruleData.enrollee().getStudyEnvironmentId())
                .portalEnvironmentId(portalEnvId)
                .customMessagesMap(customMessages)
                .retries(0)
                .build();
    }

    public NotificationContextInfo loadContextInfo(NotificationConfig config) {
        return senderMap.get(config.getDeliveryType()).loadContextInfo(config);
    }


}
