package bio.terra.pearl.api.admin.controller.notifications;

import bio.terra.pearl.api.admin.api.NotificationConfigApi;
import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.api.admin.service.notifications.NotificationConfigExtService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.notification.NotificationConfig;
import bio.terra.pearl.core.service.exception.NotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class NotificationConfigController implements NotificationConfigApi {
  private AuthUtilService authUtilService;
  private HttpServletRequest request;
  private NotificationConfigExtService notificationConfigExtService;
  private ObjectMapper objectMapper;

  public NotificationConfigController(
      AuthUtilService authUtilService,
      HttpServletRequest request,
      NotificationConfigExtService notificationConfigExtService,
      ObjectMapper objectMapper) {
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
  public ResponseEntity<Object> get(
      String portalShortcode, String studyShortcode, String envName, UUID configId) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    Optional<NotificationConfig> configOpt =
        notificationConfigExtService.find(
            operator, portalShortcode, studyShortcode, environmentName, configId);
    return ResponseEntity.ok(
        configOpt.orElseThrow(
            () -> new NotFoundException("A config with that id does not exist in this study")));
  }

  @Override
  public ResponseEntity<Object> replace(
      String portalShortcode, String studyShortcode, String envName, UUID configId, Object body) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    NotificationConfig config = objectMapper.convertValue(body, NotificationConfig.class);
    NotificationConfig newConfig =
        notificationConfigExtService.replace(
            portalShortcode, studyShortcode, environmentName, configId, config, operator);
    return ResponseEntity.ok(newConfig);
  }

  @Override
  public ResponseEntity<Object> create(
      String portalShortcode, String studyShortcode, String envName, Object body) {
    AdminUser adminUser = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    NotificationConfig config = objectMapper.convertValue(body, NotificationConfig.class);
    NotificationConfig newConfig =
        notificationConfigExtService.create(
            portalShortcode, studyShortcode, environmentName, config, adminUser);
    return ResponseEntity.ok(newConfig);
  }
}
