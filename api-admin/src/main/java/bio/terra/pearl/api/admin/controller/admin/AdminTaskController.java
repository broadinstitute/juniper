package bio.terra.pearl.api.admin.controller.admin;

import bio.terra.pearl.api.admin.api.AdminTaskApi;
import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.api.admin.service.admin.AdminTaskExtService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.workflow.AdminTask;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class AdminTaskController implements AdminTaskApi {
  private AuthUtilService authUtilService;
  private HttpServletRequest request;
  private AdminTaskExtService adminTaskExtService;
  private ObjectMapper objectMapper;

  public AdminTaskController(
      AuthUtilService authUtilService,
      HttpServletRequest request,
      AdminTaskExtService adminTaskExtService,
      ObjectMapper objectMapper) {
    this.authUtilService = authUtilService;
    this.request = request;
    this.adminTaskExtService = adminTaskExtService;
    this.objectMapper = objectMapper;
  }

  @Override
  public ResponseEntity<Object> getByStudyEnvironment(
      String portalShortcode, String studyShortcode, String envName, String include) {
    AdminUser user = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    List<String> includedRelations = List.of();
    if (!StringUtils.isBlank(include)) {
      includedRelations = List.of(include.split(","));
    }
    var tasks =
        adminTaskExtService.getByStudyEnvironment(
            portalShortcode, studyShortcode, environmentName, includedRelations, user);
    return ResponseEntity.ok(tasks);
  }

  @Override
  public ResponseEntity<Object> getByEnrollee(
      String portalShortcode, String studyShortcode, String envName, String enrolleeShortcode) {
    AdminUser user = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    var tasks = adminTaskExtService.getByEnrollee(enrolleeShortcode, user);
    return ResponseEntity.ok(tasks);
  }

  @Override
  public ResponseEntity<Object> update(
      String portalShortcode, String studyShortcode, String envName, UUID taskId, Object body) {
    AdminUser user = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    AdminTask updatedTask = objectMapper.convertValue(body, AdminTask.class);
    updatedTask.setId(taskId);
    updatedTask =
        adminTaskExtService.update(
            portalShortcode, studyShortcode, environmentName, taskId, updatedTask, user);
    return ResponseEntity.ok(updatedTask);
  }
}
