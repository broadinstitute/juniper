package bio.terra.pearl.api.participant.service;

import static bio.terra.pearl.core.model.workflow.TaskType.OUTREACH;

import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.portal.PortalWithPortalUser;
import bio.terra.pearl.core.service.survey.SurveyService;
import bio.terra.pearl.core.service.workflow.ParticipantTaskService;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SurveyExtService {
  private ParticipantTaskService participantTaskService;
  private SurveyService surveyService;
  private EnrolleeService enrolleeService;
  private AuthUtilService authUtilService;

  @Autowired
  public SurveyExtService(
      ParticipantTaskService participantTaskService,
      SurveyService surveyService,
      EnrolleeService enrolleeService,
      AuthUtilService authUtilService) {
    this.participantTaskService = participantTaskService;
    this.surveyService = surveyService;
    this.enrolleeService = enrolleeService;
    this.authUtilService = authUtilService;
  }

  // Loads all outreach activities for the logged-in PortalParticipantUser
  public List<Survey> listOutreachActivities(
      ParticipantUser user, String portalShortcode, EnvironmentName envName) {
    PortalWithPortalUser portalUser =
        authUtilService.authParticipantToPortal(user.getId(), portalShortcode, envName);
    List<Enrollee> participantEnrollees =
        enrolleeService.findByPortalParticipantUser(portalUser.ppUser());

    List<ParticipantTask> outreachTasks =
        participantEnrollees.stream()
            .map(enrollee -> participantTaskService.findByEnrolleeId(enrollee.getId()))
            .flatMap(Collection::stream)
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
