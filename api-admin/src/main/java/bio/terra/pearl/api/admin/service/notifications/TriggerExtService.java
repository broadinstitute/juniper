package bio.terra.pearl.api.admin.service.notifications;

import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.auth.EnforcePortalStudyEnvPermission;
import bio.terra.pearl.api.admin.service.auth.context.PortalStudyEnvAuthContext;
import bio.terra.pearl.core.model.notification.EmailTemplate;
import bio.terra.pearl.core.model.notification.Trigger;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.notification.NotificationContextInfo;
import bio.terra.pearl.core.service.notification.NotificationDispatcher;
import bio.terra.pearl.core.service.notification.TriggerService;
import bio.terra.pearl.core.service.notification.email.AdminEmailService;
import bio.terra.pearl.core.service.notification.email.EmailTemplateService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import bio.terra.pearl.core.service.rule.EnrolleeContext;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TriggerExtService {
  private final AdminEmailService adminEmailService;
  private final EmailTemplateService emailTemplateService;
  private final TriggerService triggerService;
  private final AuthUtilService authUtilService;
  private final StudyEnvironmentService studyEnvironmentService;
  private final PortalEnvironmentService portalEnvironmentService;
  private final NotificationDispatcher notificationDispatcher;

  public TriggerExtService(
      TriggerService triggerService,
      AuthUtilService authUtilService,
      StudyEnvironmentService studyEnvironmentService,
      PortalEnvironmentService portalEnvironmentService,
      NotificationDispatcher notificationDispatcher,
      AdminEmailService adminEmailService,
      EmailTemplateService emailTemplateService) {
    this.triggerService = triggerService;
    this.authUtilService = authUtilService;
    this.studyEnvironmentService = studyEnvironmentService;
    this.portalEnvironmentService = portalEnvironmentService;
    this.notificationDispatcher = notificationDispatcher;
    this.adminEmailService = adminEmailService;
    this.emailTemplateService = emailTemplateService;
  }

  @EnforcePortalStudyEnvPermission(permission = AuthUtilService.BASE_PERMISSON)
  public List<Trigger> findForStudy(PortalStudyEnvAuthContext authContext) {
    List<Trigger> configs =
        triggerService.findByStudyEnvironmentId(authContext.getStudyEnvironment().getId(), true);
    triggerService.attachTemplates(configs);
    return configs;
  }

  /** Gets the config specified by id, and confirms it belongs to the given portal and study */
  @EnforcePortalStudyEnvPermission(permission = AuthUtilService.BASE_PERMISSON)
  public Optional<Trigger> find(PortalStudyEnvAuthContext authContext, UUID configId) {

    Optional<Trigger> configOpt = triggerService.find(configId);
    configOpt.ifPresent(
        config -> {
          verifyTrigger(authContext, config);
          triggerService.attachTemplates(List.of(config));
        });
    return configOpt;
  }

  /**
   * tests the notification config with configId, and sends a notification to the enrollee specified
   */
  @EnforcePortalStudyEnvPermission(permission = AuthUtilService.BASE_PERMISSON)
  public void test(
      PortalStudyEnvAuthContext authContext, UUID actionId, EnrolleeContext enrolleeContext) {
    Trigger trigger =
        triggerService
            .find(actionId)
            .orElseThrow(() -> new NotFoundException("Could not find trigger"));
    verifyTrigger(authContext, trigger);

    switch (trigger.getActionType()) {
      case NOTIFICATION:
        notificationDispatcher.dispatchTestNotification(trigger, enrolleeContext);
        return;
      case ADMIN_NOTIFICATION:
        testAdminNotification(authContext, trigger, enrolleeContext);
        return;
      default:
        throw new IllegalArgumentException("Cannot test action type: " + trigger.getActionType());
    }
  }

  private void testAdminNotification(
      PortalStudyEnvAuthContext authContext, Trigger trigger, EnrolleeContext enrolleeContext) {
    EmailTemplate emailTemplate =
        emailTemplateService
            .find(trigger.getEmailTemplateId())
            .orElseThrow(() -> new NotFoundException("Email template not found"));
    emailTemplateService.attachLocalizedTemplates(emailTemplate);

    NotificationContextInfo contextInfo =
        adminEmailService.loadContextInfoForStudyEnv(
            emailTemplate, authContext.getPortal(), authContext.getStudyEnvironment().getId());

    adminEmailService.sendEmail(contextInfo, authContext.getOperator(), enrolleeContext);
  }

  /**
   * deactivates the notification config with configId, and adds a new config as specified in the
   * update object. Note though that the portalEnvironmentId and studyEnvironmentId will be set from
   * the portalShortcode and studyShortcode params. If the update contains a new email template,
   * that template will be created as well.
   */
  @Transactional
  @EnforcePortalStudyEnvPermission(permission = AuthUtilService.BASE_PERMISSON)
  public Trigger replace(PortalStudyEnvAuthContext authContext, UUID configId, Trigger update) {
    PortalEnvironment portalEnvironment =
        portalEnvironmentService
            .findOne(authContext.getPortalShortcode(), authContext.getEnvironmentName())
            .orElseThrow(() -> new NotFoundException("Could not find portal"));

    Trigger existing = triggerService.find(configId).get();
    verifyTrigger(authContext, existing);

    Trigger newConfig = create(update, authContext.getStudyEnvironment(), portalEnvironment);
    // after creating the new config, deactivate the old config
    existing.setActive(false);
    triggerService.update(existing);
    return newConfig;
  }

  /**
   * creates a new notification config. Note though that the portalEnvironmentId and
   * studyEnvironmentId will be set from the portalShortcode and studyShortcode params. If the
   * update contains a new email template, that template will be created as well.
   */
  @Transactional
  @EnforcePortalStudyEnvPermission(permission = AuthUtilService.BASE_PERMISSON)
  public Trigger create(PortalStudyEnvAuthContext authContext, Trigger newConfig) {

    PortalEnvironment portalEnvironment =
        portalEnvironmentService
            .findOne(authContext.getPortalShortcode(), authContext.getEnvironmentName())
            .orElseThrow(() -> new IllegalStateException("Could not find portal"));

    return create(newConfig, authContext.getStudyEnvironment(), portalEnvironment);
  }

  /**
   * Deletes the config specified by id, ensuring it belongs the appropriate study and environment.
   * Delete is "soft" - the entity stays in the database in order to keep records of notifications,
   * but deactivated by setting `active = false`.
   */
  @Transactional
  @EnforcePortalStudyEnvPermission(permission = AuthUtilService.BASE_PERMISSON)
  public void delete(PortalStudyEnvAuthContext authContext, UUID configId) {
    Optional<Trigger> configOpt = triggerService.find(configId);
    if (configOpt.isEmpty()) {
      throw new NotFoundException("Could not find trigger");
    }
    Trigger config = configOpt.get();

    verifyTrigger(authContext, config);

    config.setActive(false);
    triggerService.update(config);
  }

  /** confirms the given trigger is associated with the given study and portal environments */
  private void verifyTrigger(PortalStudyEnvAuthContext authContext, Trigger config) {
    StudyEnvironment studyEnvironment = authContext.getStudyEnvironment();
    PortalEnvironment portalEnvironment =
        portalEnvironmentService
            .findOne(authContext.getPortalShortcode(), authContext.getEnvironmentName())
            .orElseThrow(() -> new NotFoundException("Could not find portal"));

    if (!studyEnvironment.getId().equals(config.getStudyEnvironmentId())
        || !portalEnvironment.getId().equals(config.getPortalEnvironmentId())) {
      throw new NotFoundException("Could not find trigger");
    }
  }

  private Trigger create(
      Trigger newConfig, StudyEnvironment studyEnvironment, PortalEnvironment portalEnvironment) {
    newConfig.cleanForCopying();
    newConfig.setStudyEnvironmentId(studyEnvironment.getId());
    newConfig.setPortalEnvironmentId(portalEnvironment.getId());
    if (newConfig.getEmailTemplate() != null && newConfig.getEmailTemplateId() == null) {
      // this is a new email template, set the portal appropriately, and clear published versions
      newConfig.getEmailTemplate().setPortalId(portalEnvironment.getPortalId());
      newConfig.getEmailTemplate().cleanForCopying();
      newConfig.getEmailTemplate().setPublishedVersion(null);
    }
    Trigger savedConfig = triggerService.create(newConfig);
    return savedConfig;
  }
}
