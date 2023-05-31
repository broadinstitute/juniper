package bio.terra.pearl.api.admin.controller.notifications;

import bio.terra.pearl.api.admin.api.NotificationConfigApi;
import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.api.admin.service.notifications.NotificationConfigExtService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class NotificationConfigController implements NotificationConfigApi {
  private AuthUtilService authUtilService;
  private HttpServletRequest request;
  private NotificationConfigExtService notificationConfigExtService;

  public NotificationConfigController(
      AuthUtilService authUtilService,
      HttpServletRequest request,
      NotificationConfigExtService notificationConfigExtService) {
    this.authUtilService = authUtilService;
    this.request = request;
    this.notificationConfigExtService = notificationConfigExtService;
  }

  @Override
  public ResponseEntity<Object> findByStudy(
      String portalShortcode, String studyShortcode, String envName) {
    AdminUser adminUser = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    var configs =
        notificationConfigExtService.findForStudy(
            adminUser, portalShortcode, studyShortcode, environmentName);
    return ResponseEntity.ok(configs);
  }
}
