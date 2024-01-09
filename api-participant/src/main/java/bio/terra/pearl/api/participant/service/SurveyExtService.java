package bio.terra.pearl.api.participant.service;

import static bio.terra.pearl.core.model.workflow.TaskType.OUTREACH;

import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.service.survey.SurveyService;
import bio.terra.pearl.core.service.workflow.ParticipantTaskService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SurveyExtService {
  private ParticipantTaskService participantTaskService;
  private SurveyService surveyService;
  private AuthUtilService authUtilService;

  @Autowired
  public SurveyExtService(
      ParticipantTaskService participantTaskService,
      SurveyService surveyService,
      AuthUtilService authUtilService) {
    this.participantTaskService = participantTaskService;
    this.surveyService = surveyService;
    this.authUtilService = authUtilService;
  }

  // Loads all outreach activities for a given enrollee. This is based on their currently
  // assigned OUTREACH tasks.
  public List<Survey> listOutreachActivities(
      ParticipantUser user,
      String portalShortcode,
      EnvironmentName envName,
      String studyShortcode,
      String enrolleeShortcode) {
    authUtilService.authParticipantToPortal(user.getId(), portalShortcode, envName);
    Enrollee enrollee =
        authUtilService.authParticipantUserToEnrollee(user.getId(), enrolleeShortcode);

    List<ParticipantTask> outreachTasks =
        participantTaskService.findByEnrolleeId(enrollee.getId()).stream()
            .filter(task -> task.getTaskType().equals(OUTREACH))
            .toList();

    List<Survey> outreachSurveys =
        outreachTasks.stream()
            .map(
                task ->
                    surveyService.findByStableId(
                        task.getTargetStableId(), task.getTargetAssignedVersion()))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();

    return outreachSurveys;
  }
}
