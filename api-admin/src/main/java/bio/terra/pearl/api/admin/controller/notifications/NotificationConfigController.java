package bio.terra.pearl.api.admin.controller.notifications;

import bio.terra.pearl.api.admin.api.NotificationConfigApi;
import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.api.admin.service.notifications.NotificationConfigExtService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import javax.servlet.http.HttpServletRequest;

import bio.terra.pearl.core.model.notification.NotificationConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
public class NotificationConfigController implements NotificationConfigApi {
  private AuthUtilService authUtilService;
  private HttpServletRequest request;
  private NotificationConfigExtService notificationConfigExtService;
  private ObjectMapper objectMapper;

  public NotificationConfigController(
          AuthUtilService authUtilService,
          HttpServletRequest request,
          NotificationConfigExtService notificationConfigExtService, ObjectMapper objectMapper) {
    this.authUtilService = authUtilService;
    this.request = request;
    this.notificationConfigExtService = notificationConfigExtService;
    this.objectMapper = objectMapper;
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

  @Override
  public ResponseEntity<Object> replace(
          String portalShortcode, String studyShortcode, String envName, UUID configId, Object body ) {
    AdminUser adminUser = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    NotificationConfig config = objectMapper.convertValue(body, NotificationConfig.class);
    var configs =
            notificationConfigExtService.findForStudy(
                    adminUser, portalShortcode, studyShortcode, environmentName);
    return ResponseEntity.ok(configs);
  }
}
