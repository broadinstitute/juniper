package bio.terra.pearl.core.service.notification.email;

import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.notification.*;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.portal.PortalEnvironmentConfig;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.admin.AdminUserService;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.notification.NotificationContextInfo;
import bio.terra.pearl.core.service.notification.NotificationService;
import bio.terra.pearl.core.service.notification.substitutors.AdminEmailSubstitutor;
import bio.terra.pearl.core.service.portal.PortalEnvironmentConfigService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.core.service.rule.EnrolleeContext;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.study.StudyService;
import bio.terra.pearl.core.service.workflow.EnrolleeEvent;
import bio.terra.pearl.core.shared.ApplicationRoutingPaths;
import com.sendgrid.Mail;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
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
  private final AdminUserService adminUserService;
  private final PortalService portalService;
  private final NotificationService notificationService;
  private final PortalEnvironmentConfigService portalEnvironmentConfigService;

  public AdminEmailService(EmailTemplateService emailTemplateService,
                           ApplicationRoutingPaths routingPaths,
                           SendgridClient sendgridClient,
                           StudyEnvironmentService studyEnvironmentService,
                           StudyService studyService,
                           PortalEnvironmentService portalEnvironmentService, AdminUserService adminUserService, PortalService portalService, NotificationService notificationService, PortalEnvironmentConfigService portalEnvironmentConfigService) {
    this.emailTemplateService = emailTemplateService;
    this.routingPaths = routingPaths;
    this.sendgridClient = sendgridClient;
    this.studyEnvironmentService = studyEnvironmentService;
    this.studyService = studyService;
    this.portalEnvironmentService = portalEnvironmentService;
    this.adminUserService = adminUserService;
    this.portalService = portalService;
    this.notificationService = notificationService;
    this.portalEnvironmentConfigService = portalEnvironmentConfigService;
  }

  @Async
  public void sendWelcomeEmail(Portal portal, AdminUser adminUser) {
    NotificationContextInfo contextInfo = loadContextInfo(WELCOME_EMAIL_TEMPLATE_STABLEID, WELCOME_EMAIL_TEMPLATE_VERSION, portal);
    sendEmail(contextInfo, adminUser, null);
  }


  /**
   * Sends an admin email; if the email template is about a specific enrollee, enrolleeContext should be provided
   * so that there can be enrollee-specific substitutions in the email.
   */
  public void sendEmail(NotificationContextInfo contextInfo, AdminUser adminUser, EnrolleeContext enrolleeContext) {
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

  protected String buildAndSendEmail(LocalizedEmailTemplate localizedEmailTemplate, String adminUsername, StringSubstitutor substitutor) throws Exception {
    String fromAddress = routingPaths.getSupportEmailAddress();
    Mail mail = sendgridClient.buildEmail(localizedEmailTemplate, adminUsername, fromAddress, "Juniper", substitutor);
    return sendgridClient.sendEmail(mail);
  }

  protected Mail buildEmailToAllAdmins(LocalizedEmailTemplate localizedEmailTemplate, StringSubstitutor substitutor, List<AdminUser> adminUsers) throws Exception {

    return sendgridClient.buildMultiRecipientMail(
            localizedEmailTemplate,
            adminUsers.stream().map(AdminUser::getUsername).toList(),
            routingPaths.getSupportEmailAddress(),
            null,
            substitutor);
  }

  public void sendEmailFromTrigger(Trigger trigger, EnrolleeEvent event) throws Exception {
    Portal portal = portalService
            .findByPortalEnvironmentId(event.getPortalParticipantUser().getPortalEnvironmentId())
            .orElseThrow(() -> new IllegalStateException("Portal not found"));
    List<AdminUser> adminUsers = adminUserService.findAllWithRolesByPortal(portal.getId());

    EmailTemplate emailTemplate = emailTemplateService.find(trigger.getEmailTemplateId())
            .orElseThrow(() -> new NotFoundException("Email template not found"));
    emailTemplateService.attachLocalizedTemplates(emailTemplate);

    NotificationContextInfo contextInfo = loadContextInfoForStudyEnv(emailTemplate, portal, event.getEnrolleeContext().getEnrollee().getStudyEnvironmentId());

    StringSubstitutor substitutor = AdminEmailSubstitutor.newSubstitutor(
            null,
            contextInfo,
            routingPaths,
            event.getEnrolleeContext());


    Mail mail = buildEmailToAllAdmins(
            contextInfo.template().getTemplateForLanguage("en").orElseThrow(() -> new IllegalStateException("Failed to send admin email: expected to find an English template, but could not find it.")),
            substitutor,
            adminUsers);

    String id = sendgridClient.sendEmail(mail);

    createAdminNotification(
            event.getEnrollee(),
            trigger,
            id,
            adminUsers.stream().map(AdminUser::getUsername).toList());
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

  public NotificationContextInfo loadContextInfoForStudyEnv(EmailTemplate template, Portal portal, UUID studyEnvId) {

    StudyEnvironment studyEnvironment = studyEnvironmentService
            .find(studyEnvId)
            .orElseThrow(() -> new IllegalStateException("Study environment not found"));
    PortalEnvironment portalEnvironment = portalEnvironmentService
            .findOne(portal.getShortcode(), studyEnvironment.getEnvironmentName())
            .orElseThrow(() -> new IllegalStateException("Portal environment not found"));
    Study study = studyService
            .findByStudyEnvironmentId(studyEnvId)
            .orElseThrow(() -> new IllegalStateException("Study not found"));
    PortalEnvironmentConfig portalEnvironmentConfig = portalEnvironmentConfigService
            .findByPortalEnvId(portalEnvironment.getId())
            .orElseThrow(() -> new IllegalStateException("Portal environment config not found"));

    return new NotificationContextInfo(
            portal,
            portalEnvironment,
            portalEnvironmentConfig,
            study,
            template
    );
  }

  private void createAdminNotification(Enrollee enrollee, Trigger trigger, String sendgridApiId, List<String> sentTo) {
    Notification notification = Notification.builder()
            .enrolleeId(enrollee.getId())
            .enrollee(enrollee)
            .participantUserId(enrollee.getParticipantUserId())
            .portalEnvironmentId(trigger.getPortalEnvironmentId())
            .studyEnvironmentId(enrollee.getStudyEnvironmentId())
            .triggerId(trigger.getId())
            .deliveryStatus(NotificationDeliveryStatus.SENT)
            .deliveryType(NotificationDeliveryType.EMAIL)
            .notificationType(NotificationType.ADMIN)
            .sentTo(String.join(" ", sentTo))
            .sendgridApiRequestId(sendgridApiId)
            .retries(0)
            .build();

    notificationService.create(notification);
  }
}
