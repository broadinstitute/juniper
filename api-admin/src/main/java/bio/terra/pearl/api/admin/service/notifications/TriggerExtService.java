package bio.terra.pearl.api.admin.service.notifications;

import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.notification.Trigger;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.notification.NotificationDispatcher;
import bio.terra.pearl.core.service.notification.TriggerService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import bio.terra.pearl.core.service.portal.exception.PortalEnvironmentMissing;
import bio.terra.pearl.core.service.rule.EnrolleeContext;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.study.exception.StudyEnvironmentMissing;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TriggerExtService {
  private TriggerService triggerService;
  private AuthUtilService authUtilService;
  private StudyEnvironmentService studyEnvironmentService;
  private PortalEnvironmentService portalEnvironmentService;
  private NotificationDispatcher notificationDispatcher;

  public TriggerExtService(
      TriggerService triggerService,
      AuthUtilService authUtilService,
      StudyEnvironmentService studyEnvironmentService,
      PortalEnvironmentService portalEnvironmentService,
      NotificationDispatcher notificationDispatcher) {
    this.triggerService = triggerService;
    this.authUtilService = authUtilService;
    this.studyEnvironmentService = studyEnvironmentService;
    this.portalEnvironmentService = portalEnvironmentService;
    this.notificationDispatcher = notificationDispatcher;
  }

  public List<Trigger> findForStudy(
      AdminUser operator,
      String portalShortcode,
      String studyShortcode,
      EnvironmentName environmentName) {
    authUtilService.authUserToPortal(operator, portalShortcode);
    authUtilService.authUserToStudy(operator, portalShortcode, studyShortcode);
    StudyEnvironment studyEnvironment =
        studyEnvironmentService
            .findByStudy(studyShortcode, environmentName)
            .orElseThrow(StudyEnvironmentMissing::new);
    List<Trigger> configs = triggerService.findByStudyEnvironmentId(studyEnvironment.getId(), true);
    triggerService.attachTemplates(configs);
    return configs;
  }

  /** Gets the config specified by id, and confirms it belongs to the given portal and study */
  public Optional<Trigger> find(
      AdminUser operator,
      String portalShortcode,
      String studyShortcode,
      EnvironmentName environmentName,
      UUID configId) {
    authUtilService.authUserToPortal(operator, portalShortcode);
    authUtilService.authUserToStudy(operator, portalShortcode, studyShortcode);
    PortalEnvironment portalEnvironment =
        portalEnvironmentService
            .findOne(portalShortcode, environmentName)
            .orElseThrow(PortalEnvironmentMissing::new);
    StudyEnvironment studyEnvironment =
        studyEnvironmentService
            .findByStudy(studyShortcode, environmentName)
            .orElseThrow(StudyEnvironmentMissing::new);
    Optional<Trigger> configOpt = triggerService.find(configId);
    configOpt.ifPresent(
        config -> {
          verifyNotificationConfig(config, portalEnvironment, studyEnvironment);
          triggerService.attachTemplates(List.of(config));
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
      EnrolleeContext enrolleeContext) {
    /** find takes care of auth */
    Trigger action =
        find(operator, portalShortcode, studyShortcode, environmentName, actionId)
            .orElseThrow(() -> new NotFoundException("Could not find trigger"));
    /** for now, the only type of action this supports is sending email */
    notificationDispatcher.dispatchTestNotification(action, enrolleeContext);
  }

  /**
   * deactivates the notification config with configId, and adds a new config as specified in the
   * update object. Note though that the portalEnvironmentId and studyEnvironmentId will be set from
   * the portalShortcode and studyShortcode params. If the update contains a new email template,
   * that template will be created as well.
   */
  @Transactional
  public Trigger replace(
      String portalShortcode,
      String studyShortcode,
      EnvironmentName environmentName,
      UUID configId,
      Trigger update,
      AdminUser operator) {
    authUtilService.authUserToPortal(operator, portalShortcode);
    PortalEnvironment portalEnvironment =
        portalEnvironmentService.findOne(portalShortcode, environmentName).get();
    authUtilService.authUserToStudy(operator, portalShortcode, studyShortcode);
    StudyEnvironment studyEnvironment =
        studyEnvironmentService.findByStudy(studyShortcode, environmentName).get();
    Trigger existing = triggerService.find(configId).get();
    verifyNotificationConfig(existing, portalEnvironment, studyEnvironment);
    Trigger newConfig = create(update, studyEnvironment, portalEnvironment);
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
  public Trigger create(
      String portalShortcode,
      String studyShortcode,
      EnvironmentName environmentName,
      Trigger newConfig,
      AdminUser operator) {
    authUtilService.authUserToPortal(operator, portalShortcode);
    PortalEnvironment portalEnvironment =
        portalEnvironmentService
            .findOne(portalShortcode, environmentName)
            .orElseThrow(PortalEnvironmentMissing::new);
    authUtilService.authUserToStudy(operator, portalShortcode, studyShortcode);
    StudyEnvironment studyEnvironment =
        studyEnvironmentService
            .findByStudy(studyShortcode, environmentName)
            .orElseThrow(StudyEnvironmentMissing::new);

    return create(newConfig, studyEnvironment, portalEnvironment);
  }

  /**
   * Deletes the config specified by id, ensuring it belongs the appropriate study and environment.
   * Delete is "soft" - the entity stays in the database in order to keep records of notifications,
   * but deactivated by setting `active = false`.
   */
  @Transactional
  public void delete(
      AdminUser operator,
      String portalShortcode,
      String studyShortcode,
      EnvironmentName environmentName,
      UUID configId) {
    Optional<Trigger> configOpt =
        this.find(operator, portalShortcode, studyShortcode, environmentName, configId);
    if (configOpt.isEmpty()) {
      throw new NotFoundException("Could not find notification config.");
    }
    Trigger config = configOpt.get();

    config.setActive(false);
    triggerService.update(config);
  }

  /** confirms the given config is associated with the given study and portal environments */
  private void verifyNotificationConfig(
      Trigger config, PortalEnvironment portalEnvironment, StudyEnvironment studyEnvironment) {
    if (!studyEnvironment.getId().equals(config.getStudyEnvironmentId())
        || !portalEnvironment.getId().equals(config.getPortalEnvironmentId())) {
      throw new IllegalArgumentException("config does not match the study and portal environment");
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
