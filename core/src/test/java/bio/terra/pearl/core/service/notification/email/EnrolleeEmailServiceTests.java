package bio.terra.pearl.core.service.notification.email;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.notification.EmailTemplateFactory;
import bio.terra.pearl.core.factory.notification.NotificationFactory;
import bio.terra.pearl.core.factory.notification.TriggerFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.notification.EmailTemplate;
import bio.terra.pearl.core.model.notification.Notification;
import bio.terra.pearl.core.model.notification.NotificationDeliveryStatus;
import bio.terra.pearl.core.model.notification.NotificationDeliveryType;
import bio.terra.pearl.core.model.notification.Trigger;
import bio.terra.pearl.core.model.notification.TriggerType;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.portal.PortalEnvironmentConfig;
import bio.terra.pearl.core.service.notification.NotificationContextInfo;
import bio.terra.pearl.core.service.notification.NotificationService;
import bio.terra.pearl.core.service.rule.EnrolleeRuleData;
import bio.terra.pearl.core.service.study.StudyService;
import bio.terra.pearl.core.shared.ApplicationRoutingPaths;
import com.sendgrid.Mail;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class EnrolleeEmailServiceTests extends BaseSpringBootTest {
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private EnrolleeFactory enrolleeFactory;
    @Autowired
    private StudyService studyService;
    @Autowired
    private NotificationFactory notificationFactory;
    @Autowired
    private TriggerFactory triggerFactory;
    @Autowired
    private EmailTemplateFactory emailTemplateFactory;
    @Autowired
    private ApplicationRoutingPaths routingPaths;
    @Autowired
    private SendgridClient sendgridClient;
    @Autowired
    private EnrolleeEmailService enrolleeEmailService;


    @Test
    @Transactional
    public void testEmailBuilding() throws Exception {
        Profile profile = Profile.builder()
                .familyName("tester")
                .givenName("given")
                .contactEmail("test@test.com")
                .build();
        Enrollee enrollee = Enrollee.builder().build();
        EnrolleeRuleData ruleData = new EnrolleeRuleData(enrollee, profile, null);
        PortalEnvironmentConfig portalEnvConfig = PortalEnvironmentConfig.builder()
                .emailSourceAddress("info@portal.org").build();
        PortalEnvironment portalEnv = PortalEnvironment.builder()
                .environmentName(EnvironmentName.irb).portalEnvironmentConfig(portalEnvConfig).build();
        Portal portal = Portal.builder().shortcode("portal1").name("MyPortal").build();
        EmailTemplate emailTemplate = EmailTemplate.builder()
                    .body("family name ${profile.familyName}")
                    .subject("Welcome ${profile.givenName}").build();

        NotificationContextInfo contextInfo = new NotificationContextInfo(portal, portalEnv, portalEnvConfig, null, emailTemplate);
        Mail email = enrolleeEmailService.buildEmail(contextInfo, ruleData, new Notification());
        assertThat(email.personalization.get(0).getTos().get(0).getEmail(), equalTo("test@test.com"));
        assertThat(email.content.get(0).getValue(), equalTo("family name tester"));
        assertThat(email.from.getEmail(), equalTo("info@portal.org"));
        assertThat(email.from.getName(), equalTo("MyPortal (irb) (local)"));
        assertThat(email.getSubject(), equalTo("Welcome given"));
    }

    @Test
    @Transactional
    public void testEmailSendOrSkip(TestInfo info) {
        EnrolleeFactory.EnrolleeBundle enrolleeBundle = enrolleeFactory.buildWithPortalUser(getTestName(info));
        EmailTemplate emailTemplate = emailTemplateFactory.buildPersisted(getTestName(info), enrolleeBundle.portalId());
        Trigger config = triggerFactory.buildPersisted(Trigger.builder()
                .emailTemplateId(emailTemplate.getId())
                .deliveryType(NotificationDeliveryType.EMAIL)
                .triggerType(TriggerType.EVENT),
                enrolleeBundle.enrollee().getStudyEnvironmentId(), enrolleeBundle.portalParticipantUser().getPortalEnvironmentId());

        testSendProfile(enrolleeEmailService, enrolleeBundle, config);
        testDoNotSendProfile(enrolleeEmailService, enrolleeBundle, config);
    }

    private void testSendProfile(EnrolleeEmailService enrolleeEmailService, EnrolleeFactory.EnrolleeBundle enrolleeBundle, Trigger config) {
        Notification notification = notificationFactory.buildPersisted(enrolleeBundle, config);
        EnrolleeRuleData ruleData = new EnrolleeRuleData(enrolleeBundle.enrollee(), Profile.builder().build(), null);
        NotificationContextInfo contextInfo = new NotificationContextInfo(null, null, null, null, null);
        enrolleeEmailService.processNotification(notification, config, ruleData, contextInfo);
        Notification updatedNotification = notificationService.find(notification.getId()).get();
        // The email send should fail due to sendgrid not being configured
        assertThat(updatedNotification.getDeliveryStatus(), equalTo(NotificationDeliveryStatus.FAILED));
    }

    private void testDoNotSendProfile(EnrolleeEmailService enrolleeEmailService, EnrolleeFactory.EnrolleeBundle enrolleeBundle, Trigger config) {
        Notification notification = notificationFactory.buildPersisted(enrolleeBundle, config);
        EnrolleeRuleData ruleData = new EnrolleeRuleData(enrolleeBundle.enrollee(), Profile.builder().doNotEmail(true).build(), null);
        NotificationContextInfo contextInfo = new NotificationContextInfo(null, null, null, null, null);
        enrolleeEmailService.processNotification(notification, config, ruleData, contextInfo);
        Notification updatedNotification = notificationService.find(notification.getId()).get();
        assertThat(updatedNotification.getDeliveryStatus(), equalTo(NotificationDeliveryStatus.SKIPPED));
    }

}
