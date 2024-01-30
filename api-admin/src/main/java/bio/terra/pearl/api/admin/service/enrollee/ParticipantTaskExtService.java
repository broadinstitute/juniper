package bio.terra.pearl.api.admin.service.enrollee;

import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.workflow.DataAuditInfo;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.service.exception.internal.InternalServerException;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.study.exception.StudyEnvironmentMissing;
import bio.terra.pearl.core.service.workflow.ParticipantTaskService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ParticipantTaskExtService {
  private ParticipantTaskService participantTaskService;
  private StudyEnvironmentService studyEnvironmentService;
  private AuthUtilService authUtilService;
  private EnrolleeService enrolleeService;

  public ParticipantTaskExtService(
      ParticipantTaskService participantTaskService,
      StudyEnvironmentService studyEnvironmentService,
      AuthUtilService authUtilService,
      EnrolleeService enrolleeService) {
    this.participantTaskService = participantTaskService;
    this.studyEnvironmentService = studyEnvironmentService;
    this.authUtilService = authUtilService;
    this.enrolleeService = enrolleeService;
  }

  public List<ParticipantTask> findAll(
      String portalShortcode,
      String studyShortcode,
      EnvironmentName environmentName,
      String stableId,
      AdminUser operator) {
    authUtilService.authUserToStudy(operator, portalShortcode, studyShortcode);
    StudyEnvironment studyEnv =
        studyEnvironmentService
            .findByStudy(studyShortcode, environmentName)
            .orElseThrow(StudyEnvironmentMissing::new);
    return participantTaskService.findTasksByStudyAndTarget(studyEnv.getId(), List.of(stableId));
  }

  /**
   * applies the task updates to the given environment. Returns a list of the updated tasks This is
   * assumed to be a relatively rare operation, so this is not particularly optimized for
   * performance.
   */
  public List<ParticipantTask> updateTasks(
      String portalShortcode,
      String studyShortcode,
      EnvironmentName environmentName,
      ParticipantTaskUpdateDto updateDto,
      AdminUser operator) {
    authUtilService.authUserToStudy(operator, portalShortcode, studyShortcode);
    StudyEnvironment studyEnv =
        studyEnvironmentService
            .findByStudy(studyShortcode, environmentName)
            .orElseThrow(StudyEnvironmentMissing::new);
    List<String> targetStableIds =
        updateDto.updates().stream().map(update -> update.targetStableId()).toList();
    List<ParticipantTask> participantTasks =
        participantTaskService.findTasksByStudyAndTarget(studyEnv.getId(), targetStableIds);
    List<ParticipantTask> tasksToUpdate =
        participantTasks.stream()
            .filter(
                task ->
                    // take the task for updating if either we're updating all tasks, or if it's in
                    // the user list
                    updateDto.updateAll()
                        || updateDto
                            .portalParticipantUserIds()
                            .contains(task.getPortalParticipantUserId()))
            .toList();
    List<ParticipantTask> updatedTasks = new ArrayList<>();
    for (ParticipantTask task : tasksToUpdate) {
      ParticipantTaskUpdateDto.TaskUpdateSpec updateSpec =
          updateDto.updates().stream()
              .filter(update -> update.targetStableId().equals(task.getTargetStableId()))
              .findFirst()
              .orElseThrow(() -> new InternalServerException("unexpected query result"));
      ParticipantTask updatedTask = updateTask(task, updateSpec, operator);
      if (updatedTask != null) {
        updatedTasks.add(updatedTask);
      }
    }

    return updatedTasks;
  }

  protected ParticipantTask updateTask(
      ParticipantTask task,
      ParticipantTaskUpdateDto.TaskUpdateSpec updateSpec,
      AdminUser operator) {
    if (updateSpec.updateFromVersion() == null
        || updateSpec.updateFromVersion().equals(task.getTargetAssignedVersion())) {
      task.setTargetAssignedVersion(updateSpec.updateToVersion());
      if (updateSpec.newStatus() != null) {
        task.setStatus(updateSpec.newStatus());
      }
      DataAuditInfo auditInfo =
          DataAuditInfo.builder()
              .enrolleeId(task.getEnrolleeId())
              .portalParticipantUserId(task.getPortalParticipantUserId())
              .responsibleAdminUserId(operator.getId())
              .build();
      return participantTaskService.update(task, auditInfo);
    }
    return null;
  }
}
