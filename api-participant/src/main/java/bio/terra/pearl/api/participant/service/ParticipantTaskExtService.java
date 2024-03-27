package bio.terra.pearl.api.participant.service;

import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.form.VersionedForm;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskType;
import bio.terra.pearl.core.service.consent.ConsentFormService;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.portal.PortalWithPortalUser;
import bio.terra.pearl.core.service.survey.SurveyService;
import bio.terra.pearl.core.service.workflow.ParticipantTaskService;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ParticipantTaskExtService {
  private final ParticipantTaskService participantTaskService;
  private final SurveyService surveyService;
  private final ConsentFormService consentFormService;
  private final EnrolleeService enrolleeService;
  private final AuthUtilService authUtilService;

  @Autowired
  public ParticipantTaskExtService(
      ParticipantTaskService participantTaskService,
      SurveyService surveyService,
      ConsentFormService consentFormService,
      EnrolleeService enrolleeService,
      AuthUtilService authUtilService) {
    this.participantTaskService = participantTaskService;
    this.surveyService = surveyService;
    this.consentFormService = consentFormService;
    this.enrolleeService = enrolleeService;
    this.authUtilService = authUtilService;
  }

  public TasksAndForms listAllTasksAndForms(
      ParticipantUser user, String portalShortcode, EnvironmentName envName) {
    PortalWithPortalUser portalUser =
        authUtilService.authParticipantToPortal(user.getId(), portalShortcode, envName);
    List<Enrollee> participantEnrollees =
        enrolleeService.findByPortalParticipantUser(portalUser.ppUser());

    Map<TaskType, List<ParticipantTask>> tasksByType =
        participantTaskService
            .findByEnrolleeIds(participantEnrollees.stream().map(Enrollee::getId).toList())
            .values()
            .stream()
            .flatMap(Collection::stream)
            .collect(Collectors.groupingBy(ParticipantTask::getTaskType));

    List<TaskAndForm> researchTaskAndSurveys =
        getTasksAndFormsByType(TaskType.SURVEY, surveyService::findByStableIds, tasksByType);

    List<TaskAndForm> outreachTaskAndSurveys =
        getTasksAndFormsByType(TaskType.OUTREACH, surveyService::findByStableIds, tasksByType);

    List<TaskAndForm> consentTaskAndSurveys =
        getTasksAndFormsByType(TaskType.CONSENT, consentFormService::findByStableIds, tasksByType);

    return new TasksAndForms(researchTaskAndSurveys, outreachTaskAndSurveys, consentTaskAndSurveys);
  }

  private <F extends VersionedForm> List<TaskAndForm> getTasksAndFormsByType(
      TaskType taskType,
      BiFunction<List<String>, List<Integer>, List<F>> findByStableIds,
      Map<TaskType, List<ParticipantTask>> tasksByType) {

    List<ParticipantTask> tasks = tasksByType.getOrDefault(taskType, List.of());
    List<F> forms =
        findByStableIds.apply(
            tasks.stream().map(ParticipantTask::getTargetStableId).toList(),
            tasks.stream().map(ParticipantTask::getTargetAssignedVersion).toList());

    return IntStream.range(0, tasks.size())
        .mapToObj(i -> new TaskAndForm(forms.get(i), tasks.get(i)))
        .toList();
  }

  public record TaskAndForm(VersionedForm form, ParticipantTask task) {}

  public record TasksAndForms(
      List<TaskAndForm> surveyTasks,
      List<TaskAndForm> outreachTasks,
      List<TaskAndForm> consentTasks) {}
}
