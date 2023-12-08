package bio.terra.pearl.core.service.notification;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.kit.KitRequestStatus;
import bio.terra.pearl.core.model.notification.*;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.service.workflow.EventService;
import java.util.List;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class NotificationDispatcherTests extends BaseSpringBootTest {
    @Test
    @Transactional
    public void testEventTriggersNotificationCreation() {
        EnrolleeFactory.EnrolleeBundle enrolleeBundle = enrolleeFactory
                .buildWithPortalUser("notificationTriggers");
        NotificationConfig config = createNotificationConfig(enrolleeBundle, NotificationEventType.STUDY_ENROLLMENT);

        eventService.publishEnrolleeCreationEvent(enrolleeBundle.enrollee(), enrolleeBundle.portalParticipantUser());
        verifyNotification(config, enrolleeBundle);
    }

    @Test
    @Transactional
    void testKitSendEvent(TestInfo testInfo) {
        EnrolleeFactory.EnrolleeBundle enrolleeBundle = enrolleeFactory
                .buildWithPortalUser(getTestName(testInfo));
        NotificationConfig config = createNotificationConfig(enrolleeBundle, NotificationEventType.KIT_SENT);
        KitRequest kitRequest = KitRequest.builder()
                .status(KitRequestStatus.SENT)
                .enrolleeId(enrolleeBundle.enrollee().getId())
                .build();

        eventService.publishKitStatusEvent(kitRequest, enrolleeBundle.enrollee(), enrolleeBundle.portalParticipantUser(), KitRequestStatus.CREATED);
        verifyNotification(config, enrolleeBundle);
    }

    private NotificationConfig createNotificationConfig(EnrolleeFactory.EnrolleeBundle enrolleeBundle, NotificationEventType eventType) {
        Enrollee enrollee = enrolleeBundle.enrollee();
        NotificationConfig config = NotificationConfig.builder()
                .studyEnvironmentId(enrollee.getStudyEnvironmentId())
                .eventType(eventType)
                .deliveryType(NotificationDeliveryType.EMAIL)
                .notificationType(NotificationType.EVENT)
                .portalEnvironmentId(enrolleeBundle.portalParticipantUser().getPortalEnvironmentId())
                .build();
        config = notificationConfigService.create(config);
        return config;
    }


    private void verifyNotification(NotificationConfig config, EnrolleeFactory.EnrolleeBundle enrolleeBundle) {
        Enrollee enrollee = enrolleeBundle.enrollee();
        List<Notification> notifications = notificationService.findByEnrolleeId(enrollee.getId());
        assertThat(notifications, hasSize(1));
        Notification expectedNotification = Notification.builder()
                .notificationConfigId(config.getId())
                .deliveryType(config.getDeliveryType())
                .studyEnvironmentId(config.getStudyEnvironmentId())
                .portalEnvironmentId(enrolleeBundle.portalParticipantUser().getPortalEnvironmentId())
                .deliveryStatus(NotificationDeliveryStatus.SKIPPED) // SKIPPED since the config has no email template
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
