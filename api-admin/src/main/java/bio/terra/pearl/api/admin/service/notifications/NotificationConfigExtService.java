package bio.terra.pearl.api.admin.service.notifications;

import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.core.dao.publishing.PortalEnvironmentChangeRecordDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.notification.NotificationConfig;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.publishing.PortalEnvironmentChange;
import bio.terra.pearl.core.model.publishing.StudyEnvironmentChange;
import bio.terra.pearl.core.model.study.PortalStudy;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.notification.NotificationConfigService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import bio.terra.pearl.core.service.publishing.PortalDiffService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationConfigExtService {
  private NotificationConfigService notificationConfigService;
  private AuthUtilService authUtilService;
  private StudyEnvironmentService studyEnvironmentService;
  private PortalEnvironmentService portalEnvironmentService;
  private PortalEnvironmentChangeRecordDao portalEnvironmentChangeRecordDao;

  public NotificationConfigExtService(
          NotificationConfigService notificationConfigService,
          AuthUtilService authUtilService,
          StudyEnvironmentService studyEnvironmentService, PortalEnvironmentService portalEnvironmentService,
          PortalEnvironmentChangeRecordDao portalEnvironmentChangeRecordDao) {
    this.notificationConfigService = notificationConfigService;
    this.authUtilService = authUtilService;
    this.studyEnvironmentService = studyEnvironmentService;
    this.portalEnvironmentService = portalEnvironmentService;
    this.portalEnvironmentChangeRecordDao = portalEnvironmentChangeRecordDao;
  }

  public List<NotificationConfig> findForStudy(
      AdminUser user,
      String portalShortcode,
      String studyShortcode,
      EnvironmentName environmentName) {
    Portal portal = authUtilService.authUserToPortal(user, portalShortcode);
    PortalStudy portalStudy =
        authUtilService.authUserToStudy(user, portalShortcode, studyShortcode);
    StudyEnvironment studyEnvironment =
        studyEnvironmentService.findByStudy(studyShortcode, environmentName).get();
    var configs = notificationConfigService.findByStudyEnvironmentId(studyEnvironment.getId());
    notificationConfigService.attachTemplates(configs);
    return configs;
  }

  /**
   * deactivates the notification config with configId, and adds a new config as specified in the update object. Note though that the
   * portalEnvironmentId and studyEnvironmentId will be set from the portalShortcode and studyShortcode params.
   * If the update contains a new email template, that template will be created as well.
   */
  @Transactional
  public NotificationConfig replace(String portalShortcode, String studyShortcode, EnvironmentName environmentName, UUID configId,
                                    NotificationConfig update, AdminUser user) {
    authUtilService.authUserToPortal(user, portalShortcode);
    PortalEnvironment portalEnvironment = portalEnvironmentService.findOne(portalShortcode, environmentName).get();
    authUtilService.authUserToStudy(user, portalShortcode, studyShortcode);
    StudyEnvironment studyEnvironment =
            studyEnvironmentService.findByStudy(studyShortcode, environmentName).get();
    NotificationConfig existing = notificationConfigService.find(configId).get();

    update.cleanForCopying();
    update.setStudyEnvironmentId(studyEnvironment.getId());
    update.setPortalEnvironmentId(portalEnvironment.getId());
    if (update.getEmailTemplate() != null && update.getEmailTemplateId() == null) {
      // this is a new email template, set the portal appropriately
      update.getEmailTemplate().setPortalId(portalEnvironment.getPortalId());
    }
    NotificationConfig newConfig = notificationConfigService.create(update);

    // once the new creation succeeds, deactivate the prior config.
    existing.setActive(false);
    notificationConfigService.update(existing);
    return newConfig;
  }
}
