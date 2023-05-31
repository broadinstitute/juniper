package bio.terra.pearl.api.admin.service.notifications;

import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.notification.NotificationConfig;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.study.PortalStudy;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.notification.NotificationConfigService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class NotificationConfigExtService {
  private NotificationConfigService notificationConfigService;
  private AuthUtilService authUtilService;
  private StudyEnvironmentService studyEnvironmentService;

  public NotificationConfigExtService(
      NotificationConfigService notificationConfigService,
      AuthUtilService authUtilService,
      StudyEnvironmentService studyEnvironmentService) {
    this.notificationConfigService = notificationConfigService;
    this.authUtilService = authUtilService;
    this.studyEnvironmentService = studyEnvironmentService;
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
}
