package bio.terra.pearl.core.service.notification;

import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.notification.EmailTemplate;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.portal.PortalEnvironmentConfig;
import bio.terra.pearl.core.service.rule.EnrolleeRuleData;
import com.sendgrid.Mail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.mock.env.MockEnvironment;

public class EmailServiceTests {
    @Test
    public void testEmailBuilding() {
        Profile profile = Profile.builder()
                .familyName("tester")
                .givenName("given")
                .contactEmail("test@test.com")
                .build();
        Enrollee enrollee = Enrollee.builder().build();
        EnrolleeRuleData ruleData = EnrolleeRuleData.builder()
                .enrollee(enrollee).profile(profile).build();
        PortalEnvironmentConfig portalEnvConfig = PortalEnvironmentConfig.builder()
                .emailSourceAddress("info@portal.org").build();
        PortalEnvironment portalEnv = PortalEnvironment.builder()
                .environmentName(EnvironmentName.irb).portalEnvironmentConfig(portalEnvConfig).build();

        EmailTemplate emailTemplate = EmailTemplate.builder()
                    .body("family name ${profile.familyName}")
                    .subject("Welcome ${profile.givenName}").build();


        Environment env = new MockEnvironment().withProperty(EmailService.EMAIL_REDIRECT_VAR, "");
        EmailService emailService = new EmailService(null, null, null, env);
        Mail email = emailService.buildEmail(emailTemplate, ruleData, portalEnv, "testportal");
        assertThat(email.personalization.get(0).getTos().get(0).getEmail(), equalTo("test@test.com"));
        assertThat(email.content.get(0).getValue(), equalTo("family name tester"));
        assertThat(email.from.getEmail(), equalTo("info@portal.org"));
        assertThat(email.getSubject(), equalTo("Welcome given"));

        // now test that the to address is replaced if configured
        Environment devEnv = new MockEnvironment().withProperty(EmailService.EMAIL_REDIRECT_VAR, "developer@broad.org");
        EmailService devEmailService = new EmailService(null, null, null, devEnv);
        Mail devEmail = devEmailService.buildEmail(emailTemplate, ruleData, portalEnv, "testportal");
        assertThat(devEmail.personalization.get(0).getTos().get(0).getEmail(), equalTo("developer@broad.org"));
    }
}
