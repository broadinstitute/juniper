package bio.terra.pearl.core.service.notification.email;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.model.notification.EmailTemplate;
import bio.terra.pearl.core.model.notification.LocalizedEmailTemplate;
import bio.terra.pearl.core.service.notification.NotificationContextInfo;
import bio.terra.pearl.core.service.notification.substitutors.AdminEmailSubstitutor;
import bio.terra.pearl.core.shared.ApplicationRoutingPaths;
import com.sendgrid.Mail;
import org.apache.commons.text.StringSubstitutor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class SendgridClientTests extends BaseSpringBootTest {
  @Autowired
  private ApplicationRoutingPaths applicationRoutingPaths;

  @Test
  @Transactional
  public void testEmailBuilding() {
    Environment env = new MockEnvironment().withProperty(SendgridClient.EMAIL_REDIRECT_VAR, "")
        .withProperty("env.hostnames.adminUi", "someserver.com");
    SendgridClient sendgridClient = new SendgridClient(env, applicationRoutingPaths);
    LocalizedEmailTemplate localizedEmailTemplate = LocalizedEmailTemplate.builder()
            .body("hello ${adminUsername}")
            .language("en")
            .subject("Welcome to Juniper ${loginLink}").build();
    EmailTemplate emailTemplate = EmailTemplate.builder()
            .localizedEmailTemplates(List.of(localizedEmailTemplate)).build();
      NotificationContextInfo contextInfo = new NotificationContextInfo(null, null, null, null, emailTemplate);
    StringSubstitutor substitutor = AdminEmailSubstitutor.newSubstitutor("admin@admin.com", contextInfo, applicationRoutingPaths, null);

    Mail email = sendgridClient.buildEmail(localizedEmailTemplate, "admin@admin.com", "us@broad.org", "Broad", substitutor);
    assertThat(email.personalization.get(0).getTos().get(0).getEmail(), equalTo("admin@admin.com"));
    assertThat(email.content.get(0).getValue(), equalTo("hello admin@admin.com"));
    assertThat(email.from.getEmail(), equalTo("us@broad.org"));
    assertThat(email.getSubject(), equalTo("Welcome to Juniper <a href=\"" +
        applicationRoutingPaths.getAdminBaseUrl()
        + "\">Login to Juniper</a>"));

    // now test that the to address is replaced if configured
    Environment devEnv = new MockEnvironment().withProperty(SendgridClient.EMAIL_REDIRECT_VAR, "developer@broad.org");
    SendgridClient devSendgridClient = new SendgridClient(devEnv, applicationRoutingPaths);
    Mail devEmail = devSendgridClient.buildEmail(localizedEmailTemplate, "foo@bar.com", "us@broad.org", "Broad", substitutor);
    assertThat(devEmail.personalization.get(0).getTos().get(0).getEmail(), equalTo("developer@broad.org"));
    assertThat(devEmail.getFrom().getName(), equalTo("Broad (local)"));
  }
}
