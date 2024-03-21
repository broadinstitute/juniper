package bio.terra.pearl.populate.service;

import bio.terra.pearl.populate.service.contexts.PortalPopulateContext;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AdminConfigPopulator {
  public static final List<String> EMAILS_TO_POPULATE = List.of("adminConfig/emails/adminWelcome.json");
  private EmailTemplatePopulator emailTemplatePopulator;

  public AdminConfigPopulator(EmailTemplatePopulator emailTemplatePopulator) {
    this.emailTemplatePopulator = emailTemplatePopulator;
  }


  public AdminConfigStats populate(boolean overwrite) throws IOException {
    for (String emailFile : EMAILS_TO_POPULATE) {
      // these templates are not specific to a portal, so the shortcode and environment is null
      PortalPopulateContext context = new PortalPopulateContext(emailFile, null, null, new HashMap<>(), false, null);
      // always overwrite, we don't care about versioning for admin emails yet
//      emailTemplatePopulator.populate(context, overwrite);
    }
    return new AdminConfigStats(EMAILS_TO_POPULATE.size());
  }

  public record AdminConfigStats(int numEmails) {}
}
