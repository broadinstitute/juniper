package bio.terra.pearl.core.service.notification;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.notification.EmailTemplateFactory;
import bio.terra.pearl.core.factory.notification.NotificationConfigFactory;
import bio.terra.pearl.core.factory.notification.NotificationFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.notification.*;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.portal.PortalEnvironmentConfig;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.core.service.rule.EnrolleeRuleData;
import bio.terra.pearl.core.service.study.StudyService;
import com.sendgrid.Mail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mock.env.MockEnvironment;

public class EmailServiceTests extends BaseSpringBootTest {
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private EnrolleeFactory enrolleeFactory;
    @Autowired
    private StudyService studyService;
    @Autowired
    private NotificationFactory notificationFactory;
    @Autowired
    private NotificationConfigFactory notificationConfigFactory;
    @Autowired
    private EmailTemplateFactory emailTemplateFactory;

    @Test
    public void testEmailBuilding() {
        Profile profile = Profile.builder()
                .familyName("tester")
                .givenName("given")
                .contactEmail("test@test.com")
                .build();
        Enrollee enrollee = Enrollee.builder().build();
        EnrolleeRuleData ruleData = new EnrolleeRuleData(enrollee, profile);
        PortalEnvironmentConfig portalEnvConfig = PortalEnvironmentConfig.builder()
                .emailSourceAddress("info@portal.org").build();
        PortalEnvironment portalEnv = PortalEnvironment.builder()
                .environmentName(EnvironmentName.irb).portalEnvironmentConfig(portalEnvConfig).build();
        Portal portal = Portal.builder().shortcode("portal1").build();
        EmailTemplate emailTemplate = EmailTemplate.builder()
                    .body("family name ${profile.familyName}")
                    .subject("Welcome ${profile.givenName}").build();


        Environment env = new MockEnvironment().withProperty(EmailService.EMAIL_REDIRECT_VAR, "");
        EmailService emailService = new EmailService(env, notificationService, null, null, studyService, null);
        var contextInfo = new NotificationContextInfo(portal, portalEnv, null, emailTemplate);
        Mail email = emailService.buildEmail(contextInfo, ruleData);
        assertThat(email.personalization.get(0).getTos().get(0).getEmail(), equalTo("test@test.com"));
        assertThat(email.content.get(0).getValue(), equalTo("family name tester"));
        assertThat(email.from.getEmail(), equalTo("info@portal.org"));
        assertThat(email.getSubject(), equalTo("Welcome given"));

        // now test that the to address is replaced if configured
        Environment devEnv = new MockEnvironment().withProperty(EmailService.EMAIL_REDIRECT_VAR, "developer@broad.org");
        EmailService devEmailService = new EmailService(devEnv, notificationService, null, null, studyService, null);
        Mail devEmail = devEmailService.buildEmail(contextInfo, ruleData);
        assertThat(devEmail.personalization.get(0).getTos().get(0).getEmail(), equalTo("developer@broad.org"));
    }

    @Test
    public void testEmailSendOrSkip() {
        // set up an enrollee and valid notification config
        Environment env = new MockEnvironment().withProperty(EmailService.SENDGRID_API_KEY_VAR, "fake");
        EmailService emailService = new FakeEmailService(env, notificationService, null, null, null, null);
        EnrolleeFactory.EnrolleeBundle enrolleeBundle = enrolleeFactory.buildWithPortalUser("testShouldNotSendEmail");
        EmailTemplate emailTemplate = emailTemplateFactory.buildPersisted("testShouldNotSendEmail", enrolleeBundle.portalId());
        NotificationConfig config = notificationConfigFactory.buildPersisted(NotificationConfig.builder()
                .emailTemplateId(emailTemplate.getId())
                .deliveryType(NotificationDeliveryType.EMAIL)
                .notificationType(NotificationType.EVENT),
                enrolleeBundle.enrollee().getStudyEnvironmentId(), enrolleeBundle.portalParticipantUser().getPortalEnvironmentId());

        testSendProfile(emailService, enrolleeBundle, config);
        testDoNotSendProfile(emailService, enrolleeBundle, config);
    }

    private void testSendProfile(EmailService emailService, EnrolleeFactory.EnrolleeBundle enrolleeBundle, NotificationConfig config) {
        var notification = notificationFactory.buildPersisted(enrolleeBundle, config);
        var ruleData = new EnrolleeRuleData(enrolleeBundle.enrollee(), Profile.builder().build());
        var contextInfo = new NotificationContextInfo(null, null, null, null);
        emailService.processNotification(notification, config, ruleData, contextInfo);
        Notification updatedNotification = notificationService.find(notification.getId()).get();
        assertThat(updatedNotification.getDeliveryStatus(), equalTo(NotificationDeliveryStatus.SENT));
    }

    private void testDoNotSendProfile(EmailService emailService, EnrolleeFactory.EnrolleeBundle enrolleeBundle, NotificationConfig config) {
        Notification notification = notificationFactory.buildPersisted(enrolleeBundle, config);
        EnrolleeRuleData ruleData = new EnrolleeRuleData(enrolleeBundle.enrollee(), Profile.builder().doNotEmail(true).build());
        var contextInfo = new NotificationContextInfo(null, null, null, null);
        emailService.processNotification(notification, config, ruleData, contextInfo);
        Notification updatedNotification = notificationService.find(notification.getId()).get();
        assertThat(updatedNotification.getDeliveryStatus(), equalTo(NotificationDeliveryStatus.SKIPPED));
    }


    /** email service that doesn't actually communicate with sendgrid */
    protected class FakeEmailService extends EmailService {
        public FakeEmailService(Environment env, NotificationService notificationService,
                                PortalEnvironmentService portalEnvService, PortalService portalService,
                                StudyService studyService, EmailTemplateService emailTemplateService) {
            super(env, notificationService, portalEnvService, portalService, studyService, emailTemplateService);
        }
        @Override
        protected void sendEmail(Mail mail) {
            // do nothing
        }
        @Override
        protected void buildAndSendEmail(NotificationContextInfo contextInfo, EnrolleeRuleData ruleData) {
            // do nothing
        }
        @Override
        public Mail buildEmail(NotificationContextInfo contextInfo, EnrolleeRuleData ruleData) {
            return null;
        }
    }



}
