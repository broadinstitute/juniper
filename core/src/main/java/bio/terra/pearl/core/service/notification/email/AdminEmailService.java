package bio.terra.pearl.core.service.notification.email;

import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.notification.EmailTemplate;
import bio.terra.pearl.core.model.notification.LocalizedEmailTemplate;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.service.notification.NotificationContextInfo;
import bio.terra.pearl.core.service.notification.substitutors.AdminEmailSubstitutor;
import bio.terra.pearl.core.shared.ApplicationRoutingPaths;
import com.sendgrid.Mail;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AdminEmailService {
  private static final String WELCOME_EMAIL_TEMPLATE_STABLEID = "broad_admin_welcome";
  private static final int WELCOME_EMAIL_TEMPLATE_VERSION = 1;
  private EmailTemplateService emailTemplateService;
  private ApplicationRoutingPaths routingPaths;
  private SendgridClient sendgridClient;



  public AdminEmailService( EmailTemplateService emailTemplateService,
                           ApplicationRoutingPaths routingPaths, SendgridClient sendgridClient) {
    this.emailTemplateService = emailTemplateService;
    this.routingPaths = routingPaths;
    this.sendgridClient = sendgridClient;
  }

  @Async
  public void sendWelcomeEmail(Portal portal, AdminUser adminUser) {
    NotificationContextInfo contextInfo = loadContextInfo(WELCOME_EMAIL_TEMPLATE_STABLEID, WELCOME_EMAIL_TEMPLATE_VERSION, portal);
    sendEmail(contextInfo, adminUser);
  }

  public void sendEmail(NotificationContextInfo contextInfo, AdminUser adminUser) {
    if (!shouldSendEmail(contextInfo)) {
      return;
    }
    LocalizedEmailTemplate localizedTemplate = contextInfo.template().getTemplateForLanguage("en").get();
    StringSubstitutor substitutor = AdminEmailSubstitutor.newSubstitutor(adminUser.getUsername(), contextInfo, routingPaths);

    try {
      buildAndSendEmail(localizedTemplate, adminUser.getUsername(), substitutor);
      log.info("Email sent: adminUsername: {}, subject: {}, language: {}", adminUser.getUsername(), localizedTemplate.getSubject(), localizedTemplate.getLanguage());
    } catch (Exception e) {
      log.error("Email failed: adminUsername: {}, subject: {}, language: {}, {} ",
          adminUser.getUsername(), localizedTemplate.getSubject(), localizedTemplate.getLanguage(), e.getMessage());
    }
  }

  protected void buildAndSendEmail(LocalizedEmailTemplate localizedEmailTemplate, String adminUsername, StringSubstitutor substitutor) throws Exception {
    String fromAddress = routingPaths.getSupportEmailAddress();
    Mail mail = sendgridClient.buildEmail(localizedEmailTemplate, adminUsername, fromAddress, "Juniper", substitutor);
    sendgridClient.sendEmail(mail);
  }

  public boolean shouldSendEmail(NotificationContextInfo contextInfo) {
    if (contextInfo.template() == null) {
      log.error("no email template configured: skipping send");
      return false;
    }
    return true;
  }

  /**
   * loads the context information needed to send a notification (things not specific to a user)
   * for now, this is trivial, but later admin emails may contain other context
   */
  public NotificationContextInfo loadContextInfo(String templateStableId, int version, Portal portal) {
    EmailTemplate emailTemplate = emailTemplateService.findAdminTemplateByStableId(templateStableId, version).get();
    emailTemplateService.attachLocalizedTemplates(emailTemplate);
    return new NotificationContextInfo(
        portal,
        null,
        null,
        null,
        emailTemplate
    );
  }
}
