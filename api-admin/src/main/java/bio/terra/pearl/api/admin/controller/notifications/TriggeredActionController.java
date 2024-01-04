package bio.terra.pearl.api.admin.controller.notifications;

import bio.terra.pearl.api.admin.api.TriggeredActionApi;
import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.api.admin.service.notifications.NotificationExtService;
import bio.terra.pearl.api.admin.service.notifications.TriggeredActionExtService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.notification.TriggeredAction;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.rule.EnrolleeRuleData;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class TriggeredActionController implements TriggeredActionApi {
  private AuthUtilService authUtilService;
  private HttpServletRequest request;
  private TriggeredActionExtService triggeredActionExtService;
  private ObjectMapper objectMapper;
  private NotificationExtService notificationExtService;

  public TriggeredActionController(
      AuthUtilService authUtilService,
      HttpServletRequest request,
      TriggeredActionExtService triggeredActionExtService,
      ObjectMapper objectMapper,
      NotificationExtService notificationExtService) {
    this.authUtilService = authUtilService;
    this.request = request;
    this.triggeredActionExtService = triggeredActionExtService;
    this.objectMapper = objectMapper;
    this.notificationExtService = notificationExtService;
  }

  @Override
  public ResponseEntity<Object> findByStudy(
      String portalShortcode, String studyShortcode, String envName) {
    AdminUser adminUser = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    var configs =
        triggeredActionExtService.findForStudy(
            adminUser, portalShortcode, studyShortcode, environmentName);
    return ResponseEntity.ok(configs);
  }

  @Override
  public ResponseEntity<Object> get(
      String portalShortcode, String studyShortcode, String envName, UUID configId) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    Optional<TriggeredAction> configOpt =
        triggeredActionExtService.find(
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
    TriggeredAction config = objectMapper.convertValue(body, TriggeredAction.class);
    TriggeredAction newConfig =
        triggeredActionExtService.replace(
            portalShortcode, studyShortcode, environmentName, configId, config, operator);
    return ResponseEntity.ok(newConfig);
  }

  @Override
  public ResponseEntity<Object> test(
      String portalShortcode, String studyShortcode, String envName, UUID configId, Object body) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    EnrolleeRuleData enrolleeRuleData = objectMapper.convertValue(body, EnrolleeRuleData.class);
    triggeredActionExtService.test(
        operator,
        portalShortcode,
        studyShortcode,
        EnvironmentName.valueOfCaseInsensitive(envName),
        configId,
        enrolleeRuleData);
    return ResponseEntity.ok().build();
  }

  /** send a one-off notification. */
  @Override
  public ResponseEntity<Object> adHoc(
      String portalShortcode, String studyShortcode, String envName, Object body) {
    AdminUser adminUser = authUtilService.requireAdminUser(request);
    AdHocNotification adHoc = objectMapper.convertValue(body, AdHocNotification.class);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    TriggeredAction configUsed =
        notificationExtService.sendAdHoc(
            adminUser,
            portalShortcode,
            studyShortcode,
            environmentName,
            adHoc.enrolleeShortcodes,
            adHoc.customMessages,
            adHoc.notificationConfigId);
    return ResponseEntity.ok(configUsed);
  }

  @Override
  public ResponseEntity<Object> create(
      String portalShortcode, String studyShortcode, String envName, Object body) {
    AdminUser adminUser = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    TriggeredAction config = objectMapper.convertValue(body, TriggeredAction.class);
    TriggeredAction newConfig =
        triggeredActionExtService.create(
            portalShortcode, studyShortcode, environmentName, config, adminUser);
    return ResponseEntity.ok(newConfig);
  }

  /** object for specifying an adhoc notification. */
  public record AdHocNotification(
      List<String> enrolleeShortcodes,
      UUID notificationConfigId,
      Map<String, String> customMessages) {}
}
