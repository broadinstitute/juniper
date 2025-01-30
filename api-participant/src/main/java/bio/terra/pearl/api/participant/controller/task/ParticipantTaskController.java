package bio.terra.pearl.api.participant.controller.task;

import bio.terra.pearl.api.participant.api.ParticipantTaskApi;
import bio.terra.pearl.api.participant.service.AuthUtilService;
import bio.terra.pearl.api.participant.service.ParticipantTaskExtService;
import bio.terra.pearl.api.participant.service.RequestUtilService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.workflow.TaskType;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class ParticipantTaskController implements ParticipantTaskApi {
  private final EnrolleeService enrolleeService;
  private final ParticipantTaskExtService participantTaskExtService;
  private final RequestUtilService requestUtilService;
  private final AuthUtilService authUtilService;
  private final HttpServletRequest request;

  @Autowired
  public ParticipantTaskController(
      EnrolleeService enrolleeService,
      ParticipantTaskExtService participantTaskExtService,
      RequestUtilService requestUtilService,
      AuthUtilService authUtilService,
      HttpServletRequest request) {
    this.enrolleeService = enrolleeService;
    this.participantTaskExtService = participantTaskExtService;
    this.requestUtilService = requestUtilService;
    this.authUtilService = authUtilService;
    this.request = request;
  }

  @Override
  public ResponseEntity<Object> listTasksWithSurveys(
      String portalShortcode, String envName, String taskType, UUID participantUserId) {
    ParticipantUser user = requestUtilService.requireUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    if (participantUserId == null) {
      // default is a user getting their own tasks
      participantUserId = user.getId();
    }
    TaskType taskTypeEnum = taskType != null ? TaskType.valueOf(taskType.toUpperCase()) : null;
    List<ParticipantTaskExtService.TaskAndSurvey> outreachSurveys =
        participantTaskExtService.listSurveyTasks(
            user, portalShortcode, environmentName, taskTypeEnum, participantUserId);
    return ResponseEntity.ok(outreachSurveys);
  }
}
