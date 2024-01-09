package bio.terra.pearl.api.participant.controller.survey;

import static bio.terra.pearl.core.model.workflow.TaskType.OUTREACH;

import bio.terra.pearl.api.participant.api.OutreachApi;
import bio.terra.pearl.api.participant.service.RequestUtilService;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.survey.SurveyService;
import bio.terra.pearl.core.service.workflow.ParticipantTaskService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class SurveyController implements OutreachApi {
  private ParticipantTaskService participantTaskService;
  private SurveyService surveyService;
  private EnrolleeService enrolleeService;
  private RequestUtilService requestUtilService;
  private HttpServletRequest request;

  @Autowired
  public SurveyController(
      ParticipantTaskService participantTaskService,
      SurveyService surveyService,
      EnrolleeService enrolleeService,
      RequestUtilService requestUtilService,
      HttpServletRequest request) {
    this.participantTaskService = participantTaskService;
    this.surveyService = surveyService;
    this.enrolleeService = enrolleeService;
    this.requestUtilService = requestUtilService;
    this.request = request;
  }

  @Override
  public ResponseEntity<Object> listOutreach(
      String portalShortcode, String studyShortcode, String enrolleeShortcode) {
    ParticipantUser user = requestUtilService.requireUser(request);
    Optional<Enrollee> enrollee = enrolleeService.findByEnrolleeId(user.getId(), enrolleeShortcode);

    if (enrollee.isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    List<ParticipantTask> tasks = participantTaskService.findByEnrolleeId(enrollee.get().getId());

    List<ParticipantTask> outreachTasks =
        tasks.stream().filter(task -> task.getTaskType().equals(OUTREACH)).toList();

    System.out.println(outreachTasks.size());

    List<Optional<Survey>> outreachSurveys =
        outreachTasks.stream()
            .map(
                task ->
                    surveyService.findByStableId(
                        task.getTargetStableId(), task.getTargetAssignedVersion()))
            .toList();

    // Todo: just combine this into the previous map
    List<Survey> surveys =
        outreachSurveys.stream().filter(Optional::isPresent).map(Optional::get).toList();

    return ResponseEntity.ok(surveys);
  }
}
