package bio.terra.pearl.core.service.notification;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.kit.KitTypeFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeBundle;
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
        EnrolleeBundle enrolleeBundle = enrolleeFactory
                .buildWithPortalUser("notificationTriggers");
        Trigger config = createNotificationConfig(enrolleeBundle, TriggerEventType.STUDY_ENROLLMENT);

        eventService.publishEnrolleeCreationEvent(enrolleeBundle.enrollee(), enrolleeBundle.portalParticipantUser());
        verifyNotification(config, enrolleeBundle);
    }

    @Test
    @Transactional
    void testKitSentEvent(TestInfo testInfo) {
        String testName = getTestName(testInfo);
        EnrolleeBundle enrolleeBundle = enrolleeFactory
                .buildWithPortalUser(testName);
        Trigger config = createNotificationConfig(enrolleeBundle, TriggerEventType.KIT_SENT);
        KitRequest kitRequest = KitRequest.builder()
                .status(KitRequestStatus.SENT)
                .enrolleeId(enrolleeBundle.enrollee().getId())
                .kitType(kitTypeFactory.buildPersisted(testName))
                .build();

        eventService.publishKitStatusEvent(kitRequest, enrolleeBundle.enrollee(), enrolleeBundle.portalParticipantUser(),
                KitRequestStatus.CREATED);
        verifyNotification(config, enrolleeBundle);
    }

    @Test
    @Transactional
    void testKitReceivedEvent(TestInfo testInfo) {
        String testName = getTestName(testInfo);
        EnrolleeBundle enrolleeBundle = enrolleeFactory
                .buildWithPortalUser(testName);
        Trigger config = createNotificationConfig(enrolleeBundle, TriggerEventType.KIT_RECEIVED);
        KitRequest kitRequest = KitRequest.builder()
                .status(KitRequestStatus.RECEIVED)
                .enrolleeId(enrolleeBundle.enrollee().getId())
                .kitType(kitTypeFactory.buildPersisted(testName))
                .build();

        eventService.publishKitStatusEvent(kitRequest, enrolleeBundle.enrollee(), enrolleeBundle.portalParticipantUser(),
                KitRequestStatus.SENT);
        verifyNotification(config, enrolleeBundle);
    }

    private Trigger createNotificationConfig(EnrolleeBundle enrolleeBundle, TriggerEventType eventType) {
        Enrollee enrollee = enrolleeBundle.enrollee();
        Trigger config = Trigger.builder()
                .studyEnvironmentId(enrollee.getStudyEnvironmentId())
                .eventType(eventType)
                .deliveryType(NotificationDeliveryType.EMAIL)
                .triggerType(TriggerType.EVENT)
                .portalEnvironmentId(enrolleeBundle.portalParticipantUser().getPortalEnvironmentId())
                .build();
        config = triggerService.create(config);
        return config;
    }


    private void verifyNotification(Trigger config, EnrolleeBundle enrolleeBundle) {
        Enrollee enrollee = enrolleeBundle.enrollee();
        List<Notification> notifications = notificationService.findByEnrolleeId(enrollee.getId());
        assertThat(notifications, hasSize(1));
        Notification expectedNotification = Notification.builder()
                .triggerId(config.getId())
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
    private KitTypeFactory kitTypeFactory;
    @Autowired
    private TriggerService triggerService;
}
