package bio.terra.pearl.api.admin.service.notifications;

import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.notification.NotificationConfig;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.notification.NotificationConfigService;
import bio.terra.pearl.core.service.notification.NotificationService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import bio.terra.pearl.core.service.portal.exception.PortalEnvironmentMissing;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.study.exception.StudyEnvironmentMissing;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationConfigExtService {
  private NotificationConfigService notificationConfigService;
  private AuthUtilService authUtilService;
  private StudyEnvironmentService studyEnvironmentService;
  private PortalEnvironmentService portalEnvironmentService;
  private NotificationService notificationService;

  public NotificationConfigExtService(
      NotificationConfigService notificationConfigService,
      AuthUtilService authUtilService,
      StudyEnvironmentService studyEnvironmentService,
      PortalEnvironmentService portalEnvironmentService,
      NotificationService notificationService) {
    this.notificationConfigService = notificationConfigService;
    this.authUtilService = authUtilService;
    this.studyEnvironmentService = studyEnvironmentService;
    this.portalEnvironmentService = portalEnvironmentService;
    this.notificationService = notificationService;
  }

  public List<NotificationConfig> findForStudy(
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
    List<NotificationConfig> configs =
        notificationConfigService.findByStudyEnvironmentId(studyEnvironment.getId(), true);
    notificationConfigService.attachTemplates(configs);
    return configs;
  }

  /** Gets the config specified by id, and confirms it belongs to the given portal and study */
  public Optional<NotificationConfig> find(
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
    Optional<NotificationConfig> configOpt = notificationConfigService.find(configId);
    configOpt.ifPresent(
        config -> {
          verifyNotificationConfig(config, portalEnvironment, studyEnvironment);
          notificationConfigService.attachTemplates(List.of(config));
        });
    return configOpt;
  }

  /**
   * deactivates the notification config with configId, and adds a new config as specified in the
   * update object. Note though that the portalEnvironmentId and studyEnvironmentId will be set from
   * the portalShortcode and studyShortcode params. If the update contains a new email template,
   * that template will be created as well.
   */
  @Transactional
  public NotificationConfig replace(
      String portalShortcode,
      String studyShortcode,
      EnvironmentName environmentName,
      UUID configId,
      NotificationConfig update,
      AdminUser user) {
    authUtilService.authUserToPortal(user, portalShortcode);
    PortalEnvironment portalEnvironment =
        portalEnvironmentService.findOne(portalShortcode, environmentName).get();
    authUtilService.authUserToStudy(user, portalShortcode, studyShortcode);
    StudyEnvironment studyEnvironment =
        studyEnvironmentService.findByStudy(studyShortcode, environmentName).get();
    NotificationConfig existing = notificationConfigService.find(configId).get();
    verifyNotificationConfig(existing, portalEnvironment, studyEnvironment);
    NotificationConfig newConfig = create(update, studyEnvironment, portalEnvironment);
    // after creating the new config, deactivate the old config
    existing.setActive(false);
    notificationConfigService.update(existing);
    return newConfig;
  }

  /**
   * creates a new notification config. Note though that the portalEnvironmentId and
   * studyEnvironmentId will be set from the portalShortcode and studyShortcode params. If the
   * update contains a new email template, that template will be created as well.
   */
  @Transactional
  public NotificationConfig create(
      String portalShortcode,
      String studyShortcode,
      EnvironmentName environmentName,
      NotificationConfig newConfig,
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

  /**
   * Deletes the config specified by id, ensuring it belongs the appropriate study and environment
   */
  @Transactional
  public void delete(
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

    // make sure it exists/has the appropriate props
    Optional<NotificationConfig> configOpt = notificationConfigService.find(configId);
    if (configOpt.isEmpty()) {
      throw new NotFoundException("Could not find notification config.");
    }

    configOpt.ifPresent(
        config -> {
          // check if it's in the right portal/study env
          verifyNotificationConfig(config, portalEnvironment, studyEnvironment);
          notificationConfigService.attachTemplates(List.of(config));

          // finally, delete the notification config; however, notifications
          // must be deleted first, as they have a foreign key constraint.
          notificationService.deleteByNotificationConfigId(configId);
          notificationConfigService.delete(configId, CascadeProperty.EMPTY_SET);
        });
  }

  /** confirms the given config is associated with the given study and portal environments */
  private void verifyNotificationConfig(
      NotificationConfig config,
      PortalEnvironment portalEnvironment,
      StudyEnvironment studyEnvironment) {
    if (!studyEnvironment.getId().equals(config.getStudyEnvironmentId())
        || !portalEnvironment.getId().equals(config.getPortalEnvironmentId())) {
      throw new IllegalArgumentException("config does not match the study and portal environment");
    }
  }

  private NotificationConfig create(
      NotificationConfig newConfig,
      StudyEnvironment studyEnvironment,
      PortalEnvironment portalEnvironment) {
    newConfig.cleanForCopying();
    newConfig.setStudyEnvironmentId(studyEnvironment.getId());
    newConfig.setPortalEnvironmentId(portalEnvironment.getId());
    if (newConfig.getEmailTemplate() != null && newConfig.getEmailTemplateId() == null) {
      // this is a new email template, set the portal appropriately
      newConfig.getEmailTemplate().setPortalId(portalEnvironment.getPortalId());
    }
    NotificationConfig savedConfig = notificationConfigService.create(newConfig);
    return savedConfig;
  }
}
