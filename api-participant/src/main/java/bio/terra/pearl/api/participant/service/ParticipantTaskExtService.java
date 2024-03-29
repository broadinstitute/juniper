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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

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
        participantTaskService
            .findByEnrolleeIds(participantEnrollees.stream().map(Enrollee::getId).toList())
            .values()
            .stream()
            .flatMap(Collection::stream)
            .filter(task -> taskType == null || task.getTaskType().equals(taskType))
            .toList();

    List<Survey> outreachSurveys =
        surveyService.findByStableIds(
                outreachTasks.stream().map(ParticipantTask::getTargetStableId).toList(),
                outreachTasks.stream().map(ParticipantTask::getTargetAssignedVersion).toList(),
                Collections.nCopies(outreachTasks.size(), portalUser.portal().getId()));
    List<TaskAndSurvey> tasksAndSurveys =
        IntStream.range(0, outreachTasks.size())
            .mapToObj(i -> new TaskAndSurvey(outreachSurveys.get(i), outreachTasks.get(i)))
            .toList();
    return tasksAndSurveys;
  }

  public record TaskAndSurvey(Survey survey, ParticipantTask task) {}
}
