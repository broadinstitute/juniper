package bio.terra.pearl.api.admin.service.admin;

import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.study.PortalStudy;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.workflow.AdminTask;
import bio.terra.pearl.core.model.workflow.DataAuditInfo;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.workflow.AdminTaskService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AdminTaskExtService {
  private AuthUtilService authUtilService;
  private AdminTaskService adminTaskService;
  private StudyEnvironmentService studyEnvironmentService;

  public AdminTaskExtService(
      AuthUtilService authUtilService,
      AdminTaskService adminTaskService,
      StudyEnvironmentService studyEnvironmentService) {
    this.authUtilService = authUtilService;
    this.adminTaskService = adminTaskService;
    this.studyEnvironmentService = studyEnvironmentService;
  }

  public AdminTaskService.AdminTaskListDto getByStudyEnvironment(
      String portalShortcode,
      String studyShortcode,
      EnvironmentName environmentName,
      List<String> includedRelations,
      AdminUser user) {
    authUtilService.authUserToStudy(user, portalShortcode, studyShortcode);
    StudyEnvironment studyEnvironment =
        studyEnvironmentService.findByStudy(studyShortcode, environmentName).get();
    return adminTaskService.findByStudyEnvironmentId(studyEnvironment.getId(), includedRelations);
  }

  public List<AdminTask> getByEnrollee(String enrolleeShortcode, AdminUser user) {
    Enrollee enrollee = authUtilService.authAdminUserToEnrollee(user, enrolleeShortcode);
    return adminTaskService.findByEnrolleeId(enrollee.getId());
  }

  public AdminTask update(
      String portalShortcode,
      String studyShortcode,
      EnvironmentName envName,
      UUID taskId,
      AdminTask updatedTask,
      AdminUser user) {
    authUtilService.authUserToStudy(user, portalShortcode, studyShortcode);
    StudyEnvironment studyEnvironment =
        studyEnvironmentService.findByStudy(studyShortcode, envName).get();
    AdminTask taskToUpdate = adminTaskService.find(taskId).get();
    if (!taskToUpdate.getStudyEnvironmentId().equals(studyEnvironment.getId())) {
      throw new IllegalArgumentException("You cannot access that task from this study");
    }
    taskToUpdate.setAssignedAdminUserId(updatedTask.getAssignedAdminUserId());
    taskToUpdate.setDescription(updatedTask.getDescription());
    taskToUpdate.setDispositionNote(updatedTask.getDispositionNote());
    taskToUpdate.setStatus(updatedTask.getStatus());
    DataAuditInfo auditInfo = DataAuditInfo.fromAdminUserId(user.getId());
    return adminTaskService.update(taskToUpdate, auditInfo);
  }
}
