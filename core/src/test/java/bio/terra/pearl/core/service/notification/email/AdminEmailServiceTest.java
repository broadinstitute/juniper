package bio.terra.pearl.core.service.notification.email;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.admin.AdminUserBundle;
import bio.terra.pearl.core.factory.admin.PortalAdminUserFactory;
import bio.terra.pearl.core.factory.notification.EmailTemplateFactory;
import bio.terra.pearl.core.factory.notification.TriggerFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.notification.*;
import bio.terra.pearl.core.service.consent.EnrolleeConsentEvent;
import bio.terra.pearl.core.service.notification.NotificationService;
import bio.terra.pearl.core.service.rule.EnrolleeContext;
import bio.terra.pearl.core.service.workflow.EventService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminEmailServiceTest extends BaseSpringBootTest {
    @Autowired
    private AdminEmailService adminEmailService;
    @Autowired
    private EnrolleeFactory enrolleeFactory;
    @Autowired
    private EventService eventService;
    @Autowired
    private TriggerFactory triggerFactory;
    @Autowired
    private StudyEnvironmentFactory studyEnvironmentFactory;
    @Autowired
    private EmailTemplateFactory emailTemplateFactory;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private PortalAdminUserFactory portalAdminUserFactory;
    @Autowired
    private LocalizedEmailTemplateService localizedEmailTemplateService;

    @Test
    @Transactional
    public void testSendFromTrigger(TestInfo info) throws Exception {

        StudyEnvironmentFactory.StudyEnvironmentBundle bundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);

        EmailTemplate emailTemplate = emailTemplateFactory.buildPersisted(getTestName(info), bundle.getPortal().getId());
        localizedEmailTemplateService.create(LocalizedEmailTemplate.builder().emailTemplateId(emailTemplate.getId()).language("en").subject("subject").body("body").build());

        Trigger trigger = triggerFactory.buildPersisted(
                Trigger.builder()
                        .emailTemplateId(emailTemplate.getId())
                        .deliveryType(NotificationDeliveryType.EMAIL)
                        .actionType(TriggerActionType.ADMIN_NOTIFICATION)
                        .triggerType(TriggerType.EVENT),
                bundle.getStudyEnv().getId(),
                bundle.getPortalEnv().getId());

        EnrolleeFactory.EnrolleeBundle enrolleeBundle = enrolleeFactory.buildWithPortalUser(getTestName(info), bundle.getPortalEnv(), bundle.getStudyEnv());


        AdminUserBundle adminUserBundle1 = portalAdminUserFactory.buildPersistedWithPortals(getTestName(info), List.of(bundle.getPortal()));
        AdminUserBundle adminUserBundle2 = portalAdminUserFactory.buildPersistedWithPortals(getTestName(info), List.of(bundle.getPortal()));

        List<Notification> notificationsBefore = notificationService.findAllByConfigId(trigger.getId(), false);

        assertEquals(0, notificationsBefore.size());

        adminEmailService.sendEmailFromTrigger(
                trigger,
                EnrolleeConsentEvent
                        .builder()
                        .portalParticipantUser(enrolleeBundle.portalParticipantUser())
                        .enrollee(enrolleeBundle.enrollee())
                        .enrolleeContext(new EnrolleeContext(enrolleeBundle.enrollee(), null, enrolleeBundle.participantUser()))
                        .build());


        List<Notification> notificationsAfter = notificationService.findAllByConfigId(trigger.getId(), false);

        assertEquals(1, notificationsAfter.size());

        Notification notification = notificationsAfter.get(0);

        assertEquals(enrolleeBundle.enrollee().getId(), notification.getEnrolleeId());
        assertEquals(enrolleeBundle.enrollee().getParticipantUserId(), notification.getParticipantUserId());
        assertEquals(bundle.getPortalEnv().getId(), notification.getPortalEnvironmentId());
        assertEquals(bundle.getStudyEnv().getId(), notification.getStudyEnvironmentId());
        assertEquals(trigger.getId(), notification.getTriggerId());
        assertEquals(NotificationDeliveryStatus.SENT, notification.getDeliveryStatus());
        assertEquals(NotificationDeliveryType.EMAIL, notification.getDeliveryType());
        assertEquals(NotificationType.ADMIN, notification.getNotificationType());
        assertTrue(notification.getSentTo().contains(adminUserBundle1.user().getUsername()));
        assertTrue(notification.getSentTo().contains(adminUserBundle2.user().getUsername()));

    }
}
