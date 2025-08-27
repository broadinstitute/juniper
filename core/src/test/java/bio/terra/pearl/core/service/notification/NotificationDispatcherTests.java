package bio.terra.pearl.core.service.notification;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.kit.KitRequestFactory;
import bio.terra.pearl.core.factory.kit.KitTypeFactory;
import bio.terra.pearl.core.factory.notification.EmailTemplateFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeBundle;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.kit.KitRequestStatus;
import bio.terra.pearl.core.model.notification.*;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.service.kit.pepper.PepperKitStatus;
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
    @Autowired
    private EmailTemplateFactory emailTemplateFactory;

    @Test
    @Transactional
    public void testEventTriggersNotificationCreation() {
        EnrolleeBundle enrolleeBundle = enrolleeFactory
                .buildWithPortalUser("notificationTriggers");
        Trigger config = createNotificationConfig(enrolleeBundle, TriggerEventType.STUDY_ENROLLMENT, 2);

        eventService.publishEnrolleeCreationEvent(enrolleeBundle.enrollee(), enrolleeBundle.portalParticipantUser());
        verifyNotification(config, enrolleeBundle);
    }

    @Test
    @Transactional
    void testKitSentEvent(TestInfo testInfo) {
        String testName = getTestName(testInfo);
        EnrolleeBundle enrolleeBundle = enrolleeFactory
                .buildWithPortalUser(testName);
        Trigger config = createNotificationConfig(enrolleeBundle, TriggerEventType.KIT_SENT, 2);

        KitRequest kitRequest = kitRequestFactory.buildPersisted(testName, enrolleeBundle.enrollee(), PepperKitStatus.SENT);
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
        Trigger config = createNotificationConfig(enrolleeBundle, TriggerEventType.KIT_RECEIVED, 2);
        KitRequest kitRequest = kitRequestFactory.buildPersisted(testName, enrolleeBundle.enrollee(), PepperKitStatus.RECEIVED);

        eventService.publishKitStatusEvent(kitRequest, enrolleeBundle.enrollee(), enrolleeBundle.portalParticipantUser(),
                KitRequestStatus.SENT);
        verifyNotification( config, enrolleeBundle);
        List<Notification> notifications = notificationService.findByEnrolleeId(enrolleeBundle.enrollee().getId());
        assertThat(notifications, hasSize(1));

        // a second event should send another email
        eventService.publishKitStatusEvent(kitRequest, enrolleeBundle.enrollee(), enrolleeBundle.portalParticipantUser(),
                KitRequestStatus.SENT);
        notifications = notificationService.findByEnrolleeId(enrolleeBundle.enrollee().getId());
        assertThat(notifications, hasSize(2));
    }

    @Test
    @Transactional
    void testMaxOnEvent(TestInfo testInfo) {
        String testName = getTestName(testInfo);
        EnrolleeBundle enrolleeBundle = enrolleeFactory.buildWithPortalUser(testName);
        Trigger config = createNotificationConfig(enrolleeBundle, TriggerEventType.KIT_RECEIVED, 1);
        KitRequest kitRequest = kitRequestFactory.buildPersisted(testName, enrolleeBundle.enrollee(), PepperKitStatus.RECEIVED);
        eventService.publishKitStatusEvent(kitRequest, enrolleeBundle.enrollee(), enrolleeBundle.portalParticipantUser(),
                KitRequestStatus.SENT);
        verifyNotification(config, enrolleeBundle);

        eventService.publishKitStatusEvent(kitRequest, enrolleeBundle.enrollee(), enrolleeBundle.portalParticipantUser(),
                KitRequestStatus.SENT);
        List<Notification> notifications = notificationService.findByEnrolleeId(enrolleeBundle.enrollee().getId());
        assertThat(notifications, hasSize(1));
    }

    private Trigger createNotificationConfig(EnrolleeBundle enrolleeBundle, TriggerEventType eventType, int maxNumNotifications) {
        Enrollee enrollee = enrolleeBundle.enrollee();
        EmailTemplate template = emailTemplateFactory.buildPersisted("test", enrolleeBundle.portalId());
        Trigger config = Trigger.builder()
                .studyEnvironmentId(enrollee.getStudyEnvironmentId())
                .eventType(eventType)
                .deliveryType(NotificationDeliveryType.EMAIL)
                .triggerType(TriggerType.EVENT)
                .maxNumNotifications(maxNumNotifications)
                .emailTemplateId(template.getId())
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
    @Autowired
    private KitRequestFactory kitRequestFactory;
}
