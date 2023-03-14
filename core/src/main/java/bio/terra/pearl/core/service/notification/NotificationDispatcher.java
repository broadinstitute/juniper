package bio.terra.pearl.core.service.notification;

import bio.terra.pearl.core.model.notification.Notification;
import bio.terra.pearl.core.model.notification.NotificationConfig;
import bio.terra.pearl.core.model.notification.NotificationDeliveryStatus;
import bio.terra.pearl.core.model.notification.NotificationDeliveryType;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.service.rule.EnrolleeRuleData;
import bio.terra.pearl.core.service.rule.RuleEvaluator;
import bio.terra.pearl.core.service.workflow.DispatcherOrder;
import bio.terra.pearl.core.service.workflow.EnrolleeEvent;
import java.util.List;
import java.util.Map;
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
                .findByStudyEnvironmentId(event.getEnrollee().getStudyEnvironmentId(), true);
        for (NotificationConfig config: configs) {
            Class configClass = config.getEventType().eventClass;
            if (configClass.isInstance(event)) {
                if (RuleEvaluator.evaluateEnrolleeRule(config.getRule(), event.getEnrolleeRuleData())) {
                    Notification notification = createNotification(config, event.getEnrollee(),
                            event.getPortalParticipantUser(), event.getEnrolleeRuleData());
                    dispatchNotification(notification, config, event.getEnrolleeRuleData());
                }
            }
        }
    }

    protected void dispatchNotification(Notification notification, NotificationConfig config, EnrolleeRuleData enrolleeRuleData) {
        senderMap.get(config.getDeliveryType())
                .processNotificationAsync(notification, config, enrolleeRuleData);
    }

    public void dispatchTestNotification(NotificationConfig config, EnrolleeRuleData enrolleeRuleData) throws Exception {
        senderMap.get(config.getDeliveryType())
                .sendTestNotification(config, enrolleeRuleData);
    }

    public Notification createNotification(NotificationConfig config, Enrollee enrollee, PortalParticipantUser ppUser,
                                     EnrolleeRuleData ruleData) {
        Notification notification = Notification.builder()
                .enrolleeId(enrollee.getId())
                .participantUserId(enrollee.getParticipantUserId())
                .notificationConfigId(config.getId())
                .deliveryStatus(NotificationDeliveryStatus.READY)
                .deliveryType(config.getDeliveryType())
                .studyEnvironmentId(enrollee.getStudyEnvironmentId())
                .portalEnvironmentId(ppUser.getPortalEnvironmentId())
                .retries(0)
                .build();
        return notificationService.create(notification);
    }
}
