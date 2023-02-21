package bio.terra.pearl.core.service.notification;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.model.notification.*;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.service.workflow.EventService;
import java.util.List;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class NotificationDispatcherTests extends BaseSpringBootTest {
    @Test
    @Transactional
    public void testEventTriggersNotificationCreation() {
        EnrolleeFactory.EnrolleeWithPPUser enrolleeWithPPUser = enrolleeFactory
                .buildWithPortalUser("notificationTriggers");
        Enrollee enrollee = enrolleeWithPPUser.enrollee();
        NotificationConfig config = NotificationConfig.builder()
                .studyEnvironmentId(enrollee.getStudyEnvironmentId())
                .eventType(NotificationEventType.STUDY_ENROLLMENT)
                .deliveryType(NotificationDeliveryType.EMAIL)
                .notificationType(NotificationType.EVENT)
                .portalEnvironmentId(enrolleeWithPPUser.portalParticipantUser().getPortalEnvironmentId())
                .build();
        config = notificationConfigService.create(config);

        eventService.publishEnrolleeCreationEvent(enrollee, enrolleeWithPPUser.portalParticipantUser());

        List<Notification> notifications = notificationService.findByEnrolleeId(enrollee.getId());
        assertThat(notifications, hasSize(1));
        Notification expectedNotification = Notification.builder()
            .notificationConfigId(config.getId())
            .deliveryType(config.getDeliveryType())
            .studyEnvironmentId(config.getStudyEnvironmentId())
            .portalEnvironmentId(enrolleeWithPPUser.portalParticipantUser().getPortalEnvironmentId())
            .deliveryStatus(NotificationDeliveryStatus.READY)
            .enrolleeId(enrollee.getId())
            .participantUserId(enrollee.getParticipantUserId())
            .build();
        assertThat(notifications.get(0), samePropertyValuesAs(expectedNotification,
                "createdAt", "id", "lastUpdatedAt"));
    }
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private EventService eventService;
    @Autowired
    private EnrolleeFactory enrolleeFactory;
    @Autowired
    private NotificationConfigService notificationConfigService;
}
