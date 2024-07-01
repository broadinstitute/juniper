package bio.terra.pearl.api.admin.controller.enrollee;

import bio.terra.pearl.api.admin.api.ParticipantTaskApi;
import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.enrollee.ParticipantTaskExtService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.service.workflow.ParticipantTaskAssignDto;
import bio.terra.pearl.core.service.workflow.ParticipantTaskService;
import bio.terra.pearl.core.service.workflow.ParticipantTaskUpdateDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class ParticipantTaskController implements ParticipantTaskApi {
  private AuthUtilService authUtilService;
  private ParticipantTaskExtService participantTaskExtService;
  private HttpServletRequest request;
  private ObjectMapper objectMapper;

  public ParticipantTaskController(
      AuthUtilService authUtilService,
      ParticipantTaskExtService participantTaskExtService,
      HttpServletRequest request,
      ObjectMapper objectMapper) {
    this.authUtilService = authUtilService;
    this.participantTaskExtService = participantTaskExtService;
    this.request = request;
    this.objectMapper = objectMapper;
  }

  @Override
  public ResponseEntity<Object> updateAll(
      String portalShortcode, String studyShortcode, String envName, Object body) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    ParticipantTaskUpdateDto updateDto =
        objectMapper.convertValue(body, ParticipantTaskUpdateDto.class);
    List<ParticipantTask> participantTasks =
        participantTaskExtService.updateTasks(
            portalShortcode, studyShortcode, environmentName, updateDto, operator);
    return ResponseEntity.ok(participantTasks);
  }

  @Override
  public ResponseEntity<Object> assignToEnrollees(
      String portalShortcode, String studyShortcode, String envName, Object body) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    ParticipantTaskAssignDto assignDto =
        objectMapper.convertValue(body, ParticipantTaskAssignDto.class);
    List<ParticipantTask> participantTasks =
        participantTaskExtService.assignToEnrollees(
            portalShortcode, studyShortcode, environmentName, assignDto, operator);
    return ResponseEntity.ok(participantTasks);
  }

  @Override
  public ResponseEntity<Object> findAll(
      String portalShortcode, String studyShortcode, String envName, String targetStableId) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    List<ParticipantTask> participantTasks =
        participantTaskExtService.findAll(
            portalShortcode, studyShortcode, environmentName, targetStableId, operator);
    return ResponseEntity.ok(participantTasks);
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
    ParticipantTaskService.AdminTaskListDto tasks =
        participantTaskExtService.getByStudyEnvironment(
            portalShortcode, studyShortcode, environmentName, includedRelations, user);
    return ResponseEntity.ok(tasks);
  }

  @Override
  public ResponseEntity<Object> getByEnrollee(
      String portalShortcode, String studyShortcode, String envName, String enrolleeShortcode) {
    AdminUser user = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    List<ParticipantTask> tasks = participantTaskExtService.getByEnrollee(enrolleeShortcode, user);
    return ResponseEntity.ok(tasks);
  }

  @Override
  public ResponseEntity<Object> update(
      String portalShortcode, String studyShortcode, String envName, UUID taskId, Object body) {
    AdminUser user = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    ParticipantTask updatedTask = objectMapper.convertValue(body, ParticipantTask.class);
    updatedTask.setId(taskId);
    updatedTask =
        participantTaskExtService.update(
            portalShortcode, studyShortcode, environmentName, taskId, updatedTask, user);
    return ResponseEntity.ok(updatedTask);
  }
}
