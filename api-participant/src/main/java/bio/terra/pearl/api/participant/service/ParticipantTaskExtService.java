package bio.terra.pearl.api.participant.service;

import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskType;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.portal.PortalWithPortalUser;
import bio.terra.pearl.core.service.survey.SurveyService;
import bio.terra.pearl.core.service.workflow.ParticipantTaskService;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ParticipantTaskExtService {
  private ParticipantTaskService participantTaskService;
  private SurveyService surveyService;
  private EnrolleeService enrolleeService;
  private AuthUtilService authUtilService;

  @Autowired
  public ParticipantTaskExtService(
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
  public List<TaskAndSurvey> listSurveyTasks(
      ParticipantUser user, String portalShortcode, EnvironmentName envName, TaskType taskType) {
    PortalWithPortalUser portalUser =
        authUtilService.authParticipantToPortal(user.getId(), portalShortcode, envName);
    List<Enrollee> participantEnrollees =
        enrolleeService.findByPortalParticipantUser(portalUser.ppUser());

    List<ParticipantTask> outreachTasks =
        participantEnrollees.stream()
            .map(enrollee -> participantTaskService.findByEnrolleeId(enrollee.getId()))
            .flatMap(Collection::stream)
            .filter(task -> taskType == null || task.getTaskType().equals(taskType))
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
    List<TaskAndSurvey> tasksAndSurveys =
        IntStream.range(0, outreachTasks.size())
            .mapToObj(i -> new TaskAndSurvey(outreachSurveys.get(i), outreachTasks.get(i)))
            .toList();
    return tasksAndSurveys;
  }

  public record TaskAndSurvey(Survey survey, ParticipantTask task) {}
}
