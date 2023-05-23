package bio.terra.pearl.core.service.notification.email;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.model.notification.EmailTemplate;
import bio.terra.pearl.core.service.notification.NotificationContextInfo;
import bio.terra.pearl.core.service.notification.substitutors.AdminEmailSubstitutor;
import bio.terra.pearl.core.shared.ApplicationRoutingPaths;
import com.sendgrid.Mail;
import org.apache.commons.text.StringSubstitutor;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.transaction.annotation.Transactional;

public class SendgridClientTests extends BaseSpringBootTest {
  @Autowired
  private ApplicationRoutingPaths applicationRoutingPaths;

  @Test
  @Transactional
  public void testEmailBuilding() {
    Environment env = new MockEnvironment().withProperty(SendgridClient.EMAIL_REDIRECT_VAR, "")
        .withProperty("env.hostnames.adminUi", "someserver.com");
    SendgridClient sendgridClient = new SendgridClient(env);
    EmailTemplate emailTemplate = EmailTemplate.builder()
        .body("hello ${adminUsername}")
        .subject("Welcome to Juniper ${loginLink}").build();
    var contextInfo = new NotificationContextInfo(null, null, null, null, emailTemplate);
    StringSubstitutor substitutor = AdminEmailSubstitutor.newSubstitutor("admin@admin.com", contextInfo, applicationRoutingPaths);

    Mail email = sendgridClient.buildEmail(contextInfo, "admin@admin.com", "us@broad.org", substitutor);
    assertThat(email.personalization.get(0).getTos().get(0).getEmail(), equalTo("admin@admin.com"));
    assertThat(email.content.get(0).getValue(), equalTo("hello admin@admin.com"));
    assertThat(email.from.getEmail(), equalTo("us@broad.org"));
    assertThat(email.getSubject(), equalTo("Welcome to Juniper <a href=\"" +
        applicationRoutingPaths.getAdminBaseUrl()
        + "\">Login to Juniper</a>"));

    // now test that the to address is replaced if configured
    Environment devEnv = new MockEnvironment().withProperty(SendgridClient.EMAIL_REDIRECT_VAR, "developer@broad.org");
    SendgridClient devSendgridClient = new SendgridClient(devEnv);
    Mail devEmail = devSendgridClient.buildEmail(contextInfo, "foo@bar.com", "us@broad.org", substitutor);
    assertThat(devEmail.personalization.get(0).getTos().get(0).getEmail(), equalTo("developer@broad.org"));
  }
}
