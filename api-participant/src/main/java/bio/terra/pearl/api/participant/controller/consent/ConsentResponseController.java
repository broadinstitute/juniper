package bio.terra.pearl.api.participant.controller.consent;

import bio.terra.pearl.api.participant.api.ConsentResponseApi;
import bio.terra.pearl.api.participant.service.RequestUtilService;
import bio.terra.pearl.core.dao.consent.ConsentWithResponses;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.consent.ConsentResponseService;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class ConsentResponseController implements ConsentResponseApi {
  private ConsentResponseService consentResponseService;
  private HttpServletRequest request;
  private RequestUtilService requestUtilService;

  public ConsentResponseController(
      ConsentResponseService consentResponseService,
      HttpServletRequest request,
      RequestUtilService requestUtilService) {
    this.consentResponseService = consentResponseService;
    this.request = request;
    this.requestUtilService = requestUtilService;
  }

  @Override
  public ResponseEntity<Object> formAndResponses(
      String portalShortcode,
      String envName,
      String studyShortcode,
      String stableId,
      Integer version,
      UUID taskId) {
    /**
     * for now, we ignore the taskId. Later, we might want to validate that the task is still valid
     * before we return all the data so that the participant doesn't fill out an irrelevant form.
     * Not validating the task also makes it easier to spot-check survey and consent UX without
     * specific test users
     */
    ParticipantUser user = requestUtilService.userFromRequest(request);
    StudyEnvironment studyEnv = requestUtilService.getStudyEnv(studyShortcode, envName);
    ConsentWithResponses result =
        consentResponseService.findWithResponses(studyEnv.getId(), stableId, version, user.getId());
    return ResponseEntity.ok(result);
  }
}
