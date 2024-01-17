package bio.terra.pearl.api.participant.controller.task;

import bio.terra.pearl.api.participant.api.ParticipantTaskApi;
import bio.terra.pearl.api.participant.service.ParticipantTaskExtService;
import bio.terra.pearl.api.participant.service.RequestUtilService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.workflow.TaskType;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class ParticipantTaskController implements ParticipantTaskApi {
  private EnrolleeService enrolleeService;
  private ParticipantTaskExtService participantTaskExtService;
  private RequestUtilService requestUtilService;
  private HttpServletRequest request;

  @Autowired
  public ParticipantTaskController(
      EnrolleeService enrolleeService,
      ParticipantTaskExtService participantTaskExtService,
      RequestUtilService requestUtilService,
      HttpServletRequest request) {
    this.enrolleeService = enrolleeService;
    this.participantTaskExtService = participantTaskExtService;
    this.requestUtilService = requestUtilService;
    this.request = request;
  }

  @Override
  public ResponseEntity<Object> listTasksWithSurveys(
      String portalShortcode, String envName, String taskType) {
    ParticipantUser user = requestUtilService.requireUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    TaskType taskTypeEnum = taskType != null ? TaskType.valueOf(taskType.toUpperCase()) : null;
    List<ParticipantTaskExtService.TaskAndSurvey> outreachSurveys =
        participantTaskExtService.listSurveyTasks(
            user, portalShortcode, environmentName, taskTypeEnum);
    return ResponseEntity.ok(outreachSurveys);
  }
}
