package bio.terra.pearl.api.participant.service;

import bio.terra.common.exception.NotFoundException;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.audit.ResponsibleEntity;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.SurveyResponse;
import bio.terra.pearl.core.model.survey.SurveyWithResponse;
import bio.terra.pearl.core.model.workflow.HubResponse;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskType;
import bio.terra.pearl.core.service.portal.PortalWithPortalUser;
import bio.terra.pearl.core.service.survey.SurveyResponseService;
import bio.terra.pearl.core.service.workflow.ParticipantTaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class SurveyResponseExtService {
  private final AuthUtilService authUtilService;
  private final RequestUtilService requestUtilService;
  private final SurveyResponseService surveyResponseService;
  private final ParticipantTaskService participantTaskService;
  private final ObjectMapper objectMapper;

  /**
   * task types that are for study staff only, and so can't be viewed or completed by participants
   */
  public static final Set<TaskType> STAFF_ONLY_TASK_TYPES =
      Set.of(TaskType.ADMIN_FORM, TaskType.ADMIN_NOTE, TaskType.KIT_REQUEST);

  public SurveyResponseExtService(
      AuthUtilService authUtilService,
      RequestUtilService requestUtilService,
      SurveyResponseService surveyResponseService,
      ParticipantTaskService participantTaskService,
      ObjectMapper objectMapper) {
    this.authUtilService = authUtilService;
    this.requestUtilService = requestUtilService;
    this.surveyResponseService = surveyResponseService;
    this.participantTaskService = participantTaskService;
    this.objectMapper = objectMapper;
  }

  /**
   * returns the enrollee's task, throwing if it doesn't exist, isn't the enrollee's, or is for
   * study staff only. Staff-only tasks respond as not found, the same as tasks of other enrollees.
   */
  protected ParticipantTask requireParticipantTask(UUID taskId, Enrollee enrollee) {
    ParticipantTask task =
        participantTaskService
            .authTaskToEnrolleeId(taskId, enrollee.getId(), false)
            .orElseThrow(() -> new NotFoundException("Task not found"));
    if (STAFF_ONLY_TASK_TYPES.contains(task.getTaskType())) {
      throw new NotFoundException("Task not found");
    }
    return task;
  }

  public SurveyWithResponse findOrCreateWithActiveResponse(
      String portalShortcode,
      String studyShortcode,
      String envName,
      String stableId,
      Integer version,
      String enrolleeShortcode,
      UUID participantUserId,
      UUID taskId) {

    Enrollee enrollee =
        authUtilService.authParticipantUserToEnrollee(participantUserId, enrolleeShortcode);
    Portal portal =
        authUtilService
            .authParticipantToPortal(
                participantUserId, portalShortcode, EnvironmentName.valueOf(envName))
            .portal();
    StudyEnvironment studyEnv = requestUtilService.getStudyEnv(studyShortcode, envName);
    if (taskId != null) {
      requireParticipantTask(taskId, enrollee);
    }
    return surveyResponseService.findWithActiveResponse(
        studyEnv.getId(), portal.getId(), stableId, version, enrollee, taskId);
  }

  public HubResponse updateResponse(
      ParticipantUser user,
      String portalShortcode,
      EnvironmentName envName,
      SurveyResponse response,
      String enrolleeShortcode,
      UUID taskId) {
    Enrollee enrollee =
        authUtilService.authParticipantUserToEnrollee(user.getId(), enrolleeShortcode);
    PortalWithPortalUser portalWithOperatorUser =
        authUtilService.authParticipantToPortal(user.getId(), portalShortcode, envName);
    requireParticipantTask(taskId, enrollee);
    HubResponse result =
        surveyResponseService.updateResponse(
            response,
            new ResponsibleEntity(user),
            null,
            portalWithOperatorUser.ppUser(),
            enrollee,
            taskId,
            portalWithOperatorUser.portal().getId());
    return result;
  }
}
