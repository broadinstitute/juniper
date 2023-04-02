package bio.terra.pearl.api.participant.controller.consent;

import bio.terra.pearl.api.participant.api.ConsentResponseApi;
import bio.terra.pearl.api.participant.service.ConsentResponseExtService;
import bio.terra.pearl.api.participant.service.RequestUtilService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.consent.ConsentResponseDto;
import bio.terra.pearl.core.model.consent.ConsentWithResponses;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.workflow.HubResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class ConsentResponseController implements ConsentResponseApi {
  private ConsentResponseExtService consentResponseExtService;
  private HttpServletRequest request;
  private RequestUtilService requestUtilService;
  private ObjectMapper objectMapper;

  public ConsentResponseController(
      ConsentResponseExtService consentResponseExtService,
      HttpServletRequest request,
      RequestUtilService requestUtilService,
      ObjectMapper objectMapper) {
    this.consentResponseExtService = consentResponseExtService;
    this.request = request;
    this.requestUtilService = requestUtilService;
    this.objectMapper = objectMapper;
  }

  @Override
  public ResponseEntity<Object> formAndResponses(
      String portalShortcode,
      String envName,
      String studyShortcode,
      String enrolleeShortcode,
      String stableId,
      Integer version,
      UUID taskId) {
    /**
     * for now, we ignore the taskId. Later, we might want to validate that the task is still valid
     * before we return all the data so that the participant doesn't fill out an irrelevant form.
     * Not validating the task also makes it easier to spot-check survey and consent UX without
     * specific test users
     */
    ParticipantUser user = requestUtilService.requireUser(request);
    ConsentWithResponses consentWithResponses =
        consentResponseExtService.findWithResponses(
            studyShortcode, envName, stableId, version, enrolleeShortcode, user.getId());
    return ResponseEntity.ok(consentWithResponses);
  }

  @Override
  public ResponseEntity<Object> response(
      String portalShortcode,
      String envName,
      String studyShortcode,
      String enrolleeShortcode,
      String stableId,
      Integer version,
      UUID taskId,
      Object body) {
    ParticipantUser user = requestUtilService.requireUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    ConsentResponseDto responseDto = objectMapper.convertValue(body, ConsentResponseDto.class);
    HubResponse response =
        consentResponseExtService.submitResponse(
            portalShortcode, environmentName, enrolleeShortcode, taskId, responseDto, user.getId());
    return ResponseEntity.ok(response);
  }
}
