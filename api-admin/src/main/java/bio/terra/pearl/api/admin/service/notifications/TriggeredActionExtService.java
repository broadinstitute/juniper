package bio.terra.pearl.api.admin.service.notifications;

import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.notification.TriggeredAction;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.notification.NotificationDispatcher;
import bio.terra.pearl.core.service.notification.TriggeredActionService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import bio.terra.pearl.core.service.portal.exception.PortalEnvironmentMissing;
import bio.terra.pearl.core.service.rule.EnrolleeRuleData;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.study.exception.StudyEnvironmentMissing;
import jakarta.ws.rs.NotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TriggeredActionExtService {
  private TriggeredActionService triggeredActionService;
  private AuthUtilService authUtilService;
  private StudyEnvironmentService studyEnvironmentService;
  private PortalEnvironmentService portalEnvironmentService;
  private NotificationDispatcher notificationDispatcher;

  public TriggeredActionExtService(
      TriggeredActionService triggeredActionService,
      AuthUtilService authUtilService,
      StudyEnvironmentService studyEnvironmentService,
      PortalEnvironmentService portalEnvironmentService,
      NotificationDispatcher notificationDispatcher) {
    this.triggeredActionService = triggeredActionService;
    this.authUtilService = authUtilService;
    this.studyEnvironmentService = studyEnvironmentService;
    this.portalEnvironmentService = portalEnvironmentService;
    this.notificationDispatcher = notificationDispatcher;
  }

  public List<TriggeredAction> findForStudy(
      AdminUser user,
      String portalShortcode,
      String studyShortcode,
      EnvironmentName environmentName) {
    authUtilService.authUserToPortal(user, portalShortcode);
    authUtilService.authUserToStudy(user, portalShortcode, studyShortcode);
    StudyEnvironment studyEnvironment =
        studyEnvironmentService
            .findByStudy(studyShortcode, environmentName)
            .orElseThrow(StudyEnvironmentMissing::new);
    List<TriggeredAction> configs =
        triggeredActionService.findByStudyEnvironmentId(studyEnvironment.getId(), true);
    triggeredActionService.attachTemplates(configs);
    return configs;
  }

  /** Gets the config specified by id, and confirms it belongs to the given portal and study */
  public Optional<TriggeredAction> find(
      AdminUser user,
      String portalShortcode,
      String studyShortcode,
      EnvironmentName environmentName,
      UUID configId) {
    authUtilService.authUserToPortal(user, portalShortcode);
    authUtilService.authUserToStudy(user, portalShortcode, studyShortcode);
    PortalEnvironment portalEnvironment =
        portalEnvironmentService
            .findOne(portalShortcode, environmentName)
            .orElseThrow(PortalEnvironmentMissing::new);
    StudyEnvironment studyEnvironment =
        studyEnvironmentService
            .findByStudy(studyShortcode, environmentName)
            .orElseThrow(StudyEnvironmentMissing::new);
    Optional<TriggeredAction> configOpt = triggeredActionService.find(configId);
    configOpt.ifPresent(
        config -> {
          verifyNotificationConfig(config, portalEnvironment, studyEnvironment);
          triggeredActionService.attachTemplates(List.of(config));
        });
    return configOpt;
  }

  /**
   * tests the notification config with configId, and sends a notification to the enrollee specified
   */
  public void test(
      AdminUser operator,
      String portalShortcode,
      String studyShortcode,
      EnvironmentName environmentName,
      UUID actionId,
      EnrolleeRuleData enrolleeRuleData) {
    /** find takes care of auth */
    TriggeredAction action =
        find(operator, portalShortcode, studyShortcode, environmentName, actionId)
            .orElseThrow(NotFoundException::new);
    /** for now, the only type of action this supports is sending email */
    notificationDispatcher.dispatchTestNotification(action, enrolleeRuleData);
  }

  /**
   * deactivates the notification config with configId, and adds a new config as specified in the
   * update object. Note though that the portalEnvironmentId and studyEnvironmentId will be set from
   * the portalShortcode and studyShortcode params. If the update contains a new email template,
   * that template will be created as well.
   */
  @Transactional
  public TriggeredAction replace(
      String portalShortcode,
      String studyShortcode,
      EnvironmentName environmentName,
      UUID configId,
      TriggeredAction update,
      AdminUser user) {
    authUtilService.authUserToPortal(user, portalShortcode);
    PortalEnvironment portalEnvironment =
        portalEnvironmentService.findOne(portalShortcode, environmentName).get();
    authUtilService.authUserToStudy(user, portalShortcode, studyShortcode);
    StudyEnvironment studyEnvironment =
        studyEnvironmentService.findByStudy(studyShortcode, environmentName).get();
    TriggeredAction existing = triggeredActionService.find(configId).get();
    verifyNotificationConfig(existing, portalEnvironment, studyEnvironment);
    TriggeredAction newConfig = create(update, studyEnvironment, portalEnvironment);
    // after creating the new config, deactivate the old config
    existing.setActive(false);
    triggeredActionService.update(existing);
    return newConfig;
  }

  /**
   * creates a new notification config. Note though that the portalEnvironmentId and
   * studyEnvironmentId will be set from the portalShortcode and studyShortcode params. If the
   * update contains a new email template, that template will be created as well.
   */
  @Transactional
  public TriggeredAction create(
      String portalShortcode,
      String studyShortcode,
      EnvironmentName environmentName,
      TriggeredAction newConfig,
      AdminUser user) {
    authUtilService.authUserToPortal(user, portalShortcode);
    PortalEnvironment portalEnvironment =
        portalEnvironmentService
            .findOne(portalShortcode, environmentName)
            .orElseThrow(PortalEnvironmentMissing::new);
    authUtilService.authUserToStudy(user, portalShortcode, studyShortcode);
    StudyEnvironment studyEnvironment =
        studyEnvironmentService
            .findByStudy(studyShortcode, environmentName)
            .orElseThrow(StudyEnvironmentMissing::new);

    return create(newConfig, studyEnvironment, portalEnvironment);
  }

  /** confirms the given config is associated with the given study and portal environments */
  private void verifyNotificationConfig(
      TriggeredAction config,
      PortalEnvironment portalEnvironment,
      StudyEnvironment studyEnvironment) {
    if (!studyEnvironment.getId().equals(config.getStudyEnvironmentId())
        || !portalEnvironment.getId().equals(config.getPortalEnvironmentId())) {
      throw new IllegalArgumentException("config does not match the study and portal environment");
    }
  }

  private TriggeredAction create(
      TriggeredAction newConfig,
      StudyEnvironment studyEnvironment,
      PortalEnvironment portalEnvironment) {
    newConfig.cleanForCopying();
    newConfig.setStudyEnvironmentId(studyEnvironment.getId());
    newConfig.setPortalEnvironmentId(portalEnvironment.getId());
    if (newConfig.getEmailTemplate() != null && newConfig.getEmailTemplateId() == null) {
      // this is a new email template, set the portal appropriately
      newConfig.getEmailTemplate().setPortalId(portalEnvironment.getPortalId());
    }
    TriggeredAction savedConfig = triggeredActionService.create(newConfig);
    return savedConfig;
  }
}
