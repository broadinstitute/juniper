package bio.terra.pearl.core.service.notification;

import bio.terra.pearl.core.model.notification.Notification;
import bio.terra.pearl.core.model.notification.NotificationDeliveryStatus;
import bio.terra.pearl.core.model.notification.NotificationDeliveryType;
import bio.terra.pearl.core.model.notification.Trigger;
import bio.terra.pearl.core.model.notification.TriggerType;
import bio.terra.pearl.core.service.notification.email.EnrolleeEmailService;
import bio.terra.pearl.core.service.rule.EnrolleeProfileBundle;
import bio.terra.pearl.core.service.rule.EnrolleeRuleEvaluator;
import bio.terra.pearl.core.service.workflow.DispatcherOrder;
import bio.terra.pearl.core.service.workflow.EnrolleeEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class NotificationDispatcher {
    private TriggerService triggerService;
    private NotificationService notificationService;
    private Map<NotificationDeliveryType, NotificationSender> senderMap;

    public NotificationDispatcher(TriggerService triggerService,
                                  NotificationService notificationService, EnrolleeEmailService enrolleeEmailService) {
        this.triggerService = triggerService;
        this.notificationService = notificationService;
        senderMap = Map.of(NotificationDeliveryType.EMAIL, enrolleeEmailService);
    }

    /** notifications could be triggered by just about anything, so listen to all enrollee events */
    @EventListener
    @Order(DispatcherOrder.NOTIFICATION)
    public void handleEvent(EnrolleeEvent event) {
        List<Trigger> configs = triggerService
                .findByStudyEnvironmentId(event.getEnrollee().getStudyEnvironmentId(), true)
                .stream().filter(config  -> config.getTriggerType().equals(TriggerType.EVENT))
                .toList();
        for (Trigger config: configs) {
            Class configClass = config.getEventType().eventClass;
            if (configClass.isInstance(event)) {
                if (EnrolleeRuleEvaluator.evaluateRule(config.getRule(), event.getEnrolleeProfileBundle())) {
                    dispatchNotificationAsync(config, event.getEnrolleeProfileBundle(),
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
    protected void dispatchNotificationAsync(Trigger config, EnrolleeProfileBundle enrolleeProfileBundle, UUID portalEnvId) {
        Notification notification = initializeNotification(config, enrolleeProfileBundle, portalEnvId, null);
        notification = notificationService.create(notification);
        senderMap.get(config.getDeliveryType())
                .processNotificationAsync(notification, config, enrolleeProfileBundle);
    }

    public void dispatchNotification(Trigger config, EnrolleeProfileBundle enrolleeProfileBundle,
                                     NotificationContextInfo notificationContextInfo) {
        dispatchNotification(config, enrolleeProfileBundle, notificationContextInfo, Map.of());
    }

    public void dispatchNotification(Trigger config, EnrolleeProfileBundle enrolleeProfileBundle,
                                     NotificationContextInfo notificationContextInfo, Map<String, String> customMessages) {
        Notification notification = initializeNotification(config, enrolleeProfileBundle,
            notificationContextInfo.portalEnv().getId(), customMessages);
        senderMap.get(config.getDeliveryType())
                .processNotification(notification, config, enrolleeProfileBundle, notificationContextInfo);
    }

    public void dispatchTestNotification(Trigger config, EnrolleeProfileBundle enrolleeProfileBundle) {
        senderMap.get(config.getDeliveryType())
                .sendTestNotification(config, enrolleeProfileBundle);
    }

    public Notification initializeNotification(Trigger config, EnrolleeProfileBundle ruleData,
                                               UUID portalEnvId, Map<String, String> customMessages) {
        return Notification.builder()
                .enrolleeId(ruleData.getEnrollee().getId())
                .participantUserId(ruleData.getEnrollee().getParticipantUserId())
                .triggerId(config.getId())
                .deliveryStatus(NotificationDeliveryStatus.READY)
                .deliveryType(config.getDeliveryType())
                .studyEnvironmentId(ruleData.getEnrollee().getStudyEnvironmentId())
                .portalEnvironmentId(portalEnvId)
                .customMessagesMap(customMessages)
                .retries(0)
                .build();
    }

    public NotificationContextInfo loadContextInfo(Trigger config) {
        return senderMap.get(config.getDeliveryType()).loadContextInfo(config);
    }


}
