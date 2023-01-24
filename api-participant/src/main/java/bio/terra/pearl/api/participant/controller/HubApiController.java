package bio.terra.pearl.api.participant.controller;

import bio.terra.pearl.api.participant.service.RequestUtilService;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

/** API for big blobs of stuff needed to show the participant hub */
public class HubApiController {
  private RequestUtilService requestUtilService;
  private HttpServletRequest request;

  public HubApiController(RequestUtilService requestUtilService, HttpServletRequest request) {
    this.requestUtilService = requestUtilService;
    this.request = request;
  }

  public ResponseEntity<Object> get(String portalShortcode, String envName) {
    ParticipantUser user = requestUtilService.getFromRequest(request);
    return null;
  }

  public static record HubPacket(
      Set<Enrollee> enrollees, Set<StudyEnvironment> studyEnvironments) {}
}
