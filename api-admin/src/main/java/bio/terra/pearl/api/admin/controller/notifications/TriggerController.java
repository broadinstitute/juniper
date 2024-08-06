package bio.terra.pearl.api.admin.controller.notifications;

import bio.terra.pearl.api.admin.api.TriggerApi;
import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.auth.context.PortalStudyEnvAuthContext;
import bio.terra.pearl.api.admin.service.notifications.NotificationExtService;
import bio.terra.pearl.api.admin.service.notifications.TriggerExtService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.notification.Trigger;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.rule.EnrolleeContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class TriggerController implements TriggerApi {
  private AuthUtilService authUtilService;
  private HttpServletRequest request;
  private TriggerExtService triggerExtService;
  private ObjectMapper objectMapper;
  private NotificationExtService notificationExtService;

  public TriggerController(
      AuthUtilService authUtilService,
      HttpServletRequest request,
      TriggerExtService triggerExtService,
      ObjectMapper objectMapper,
      NotificationExtService notificationExtService) {
    this.authUtilService = authUtilService;
    this.request = request;
    this.triggerExtService = triggerExtService;
    this.objectMapper = objectMapper;
    this.notificationExtService = notificationExtService;
  }

  @Override
  public ResponseEntity<Object> findByStudy(
      String portalShortcode, String studyShortcode, String envName) {
    AdminUser adminUser = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    List<Trigger> configs =
        triggerExtService.findForStudy(
            PortalStudyEnvAuthContext.of(
                adminUser, portalShortcode, studyShortcode, environmentName));
    return ResponseEntity.ok(configs);
  }

  @Override
  public ResponseEntity<Object> get(
      String portalShortcode, String studyShortcode, String envName, UUID configId) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    Optional<Trigger> configOpt =
        triggerExtService.find(
            PortalStudyEnvAuthContext.of(
                operator, portalShortcode, studyShortcode, environmentName),
            configId);
    return ResponseEntity.ok(
        configOpt.orElseThrow(
            () -> new NotFoundException("A config with that id does not exist in this study")));
  }

  @Override
  public ResponseEntity<Object> replace(
      String portalShortcode, String studyShortcode, String envName, UUID configId, Object body) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    Trigger config = objectMapper.convertValue(body, Trigger.class);
    Trigger newConfig =
        triggerExtService.replace(
            PortalStudyEnvAuthContext.of(
                operator, portalShortcode, studyShortcode, environmentName),
            configId,
            config);
    return ResponseEntity.ok(newConfig);
  }

  @Override
  public ResponseEntity<Object> test(
      String portalShortcode, String studyShortcode, String envName, UUID configId, Object body) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    EnrolleeContext enrolleeContext = objectMapper.convertValue(body, EnrolleeContext.class);
    triggerExtService.test(
        PortalStudyEnvAuthContext.of(
            operator,
            portalShortcode,
            studyShortcode,
            EnvironmentName.valueOfCaseInsensitive(envName)),
        configId,
        enrolleeContext);
    return ResponseEntity.ok().build();
  }

  /** send a one-off notification. */
  @Override
  public ResponseEntity<Object> adHoc(
      String portalShortcode, String studyShortcode, String envName, Object body) {
    AdminUser adminUser = authUtilService.requireAdminUser(request);
    AdHocNotification adHoc = objectMapper.convertValue(body, AdHocNotification.class);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    Trigger configUsed =
        notificationExtService.sendAdHoc(
            adminUser,
            portalShortcode,
            studyShortcode,
            environmentName,
            adHoc.enrolleeShortcodes,
            adHoc.customMessages,
            adHoc.triggerId);
    return ResponseEntity.ok(configUsed);
  }

  @Override
  public ResponseEntity<Object> create(
      String portalShortcode, String studyShortcode, String envName, Object body) {
    AdminUser adminUser = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    Trigger config = objectMapper.convertValue(body, Trigger.class);
    Trigger newConfig =
        triggerExtService.create(
            PortalStudyEnvAuthContext.of(
                adminUser, portalShortcode, studyShortcode, environmentName),
            config);
    return ResponseEntity.ok(newConfig);
  }

  @Override
  public ResponseEntity<Void> delete(
      String portalShortcode, String studyShortcode, String envName, UUID configId) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    triggerExtService.delete(
        PortalStudyEnvAuthContext.of(operator, portalShortcode, studyShortcode, environmentName),
        configId);

    return ResponseEntity.noContent().build();
  }

  /** object for specifying an adhoc notification. */
  public record AdHocNotification(
      List<String> enrolleeShortcodes, UUID triggerId, Map<String, String> customMessages) {}
}
