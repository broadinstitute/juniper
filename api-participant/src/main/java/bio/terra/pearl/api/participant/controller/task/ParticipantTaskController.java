package bio.terra.pearl.api.participant.controller.task;

import bio.terra.pearl.api.participant.api.ParticipantTaskApi;
import bio.terra.pearl.api.participant.service.ParticipantTaskExtService;
import bio.terra.pearl.api.participant.service.RequestUtilService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class ParticipantTaskController implements ParticipantTaskApi {
  private final ParticipantTaskExtService participantTaskExtService;
  private final RequestUtilService requestUtilService;
  private final HttpServletRequest request;

  @Autowired
  public ParticipantTaskController(
      ParticipantTaskExtService participantTaskExtService,
      RequestUtilService requestUtilService,
      HttpServletRequest request) {
    this.participantTaskExtService = participantTaskExtService;
    this.requestUtilService = requestUtilService;
    this.request = request;
  }

  @Override
  public ResponseEntity<Object> listTasksWithSurveys(String portalShortcode, String envName) {
    ParticipantUser user = requestUtilService.requireUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);

    return ResponseEntity.ok(
        participantTaskExtService.listAllTasksAndForms(user, portalShortcode, environmentName));
  }
}
