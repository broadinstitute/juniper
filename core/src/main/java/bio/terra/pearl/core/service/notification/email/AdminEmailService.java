package bio.terra.pearl.core.service.notification.email;

import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.notification.EmailTemplate;
import bio.terra.pearl.core.model.notification.LocalizedEmailTemplate;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.notification.NotificationContextInfo;
import bio.terra.pearl.core.service.notification.substitutors.AdminEmailSubstitutor;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import bio.terra.pearl.core.service.rule.EnrolleeContext;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.study.StudyService;
import bio.terra.pearl.core.shared.ApplicationRoutingPaths;
import com.sendgrid.Mail;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.UUID;

@Service
@Slf4j
public class AdminEmailService {
  private static final String WELCOME_EMAIL_TEMPLATE_STABLEID = "broad_admin_welcome";
  private static final int WELCOME_EMAIL_TEMPLATE_VERSION = 1;
  private final StudyEnvironmentService studyEnvironmentService;
  private final StudyService studyService;
  private final PortalEnvironmentService portalEnvironmentService;
  private final EmailTemplateService emailTemplateService;
  private final ApplicationRoutingPaths routingPaths;
  private final SendgridClient sendgridClient;

  public AdminEmailService(EmailTemplateService emailTemplateService,
                           ApplicationRoutingPaths routingPaths,
                           SendgridClient sendgridClient,
                           StudyEnvironmentService studyEnvironmentService,
                           StudyService studyService,
                           PortalEnvironmentService portalEnvironmentService) {
    this.emailTemplateService = emailTemplateService;
    this.routingPaths = routingPaths;
    this.sendgridClient = sendgridClient;
    this.studyEnvironmentService = studyEnvironmentService;
    this.studyService = studyService;
    this.portalEnvironmentService = portalEnvironmentService;
  }

  @Async
  public void sendWelcomeEmail(Portal portal, AdminUser adminUser) {
    NotificationContextInfo contextInfo = loadContextInfo(WELCOME_EMAIL_TEMPLATE_STABLEID, WELCOME_EMAIL_TEMPLATE_VERSION, portal);
    sendEmail(contextInfo, adminUser, null);
  }

  public void sendEmail(NotificationContextInfo contextInfo, AdminUser adminUser, @Nullable EnrolleeContext enrolleeContext) {
    if (!shouldSendEmail(contextInfo)) {
      return;
    }
    LocalizedEmailTemplate localizedTemplate = contextInfo.template().getTemplateForLanguage("en").get();
    StringSubstitutor substitutor = AdminEmailSubstitutor.newSubstitutor(adminUser.getUsername(), contextInfo, routingPaths, enrolleeContext);

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

  public NotificationContextInfo loadContextInfoFromEnrollee(EmailTemplate template, Portal portal, UUID studyEnvId) {

    StudyEnvironment studyEnvironment = studyEnvironmentService
            .find(studyEnvId)
            .orElseThrow(() -> new IllegalStateException("Study environment not found"));
    PortalEnvironment portalEnvironment = portalEnvironmentService
            .findOne(portal.getShortcode(), studyEnvironment.getEnvironmentName())
            .orElseThrow(() -> new IllegalStateException("Portal environment not found"));
    Study study = studyService
            .findByStudyEnvironmentId(studyEnvId)
            .orElseThrow(() -> new IllegalStateException("Study not found"));

    return new NotificationContextInfo(
            portal,
            portalEnvironment,
            portalEnvironment.getPortalEnvironmentConfig(),
            study,
            template
    );

  }
}
