package bio.terra.pearl.api.participant.controller.registration;

import bio.terra.pearl.api.participant.api.PreregistrationApi;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.survey.ParsedPreRegResponse;
import bio.terra.pearl.core.model.survey.PreregistrationResponse;
import bio.terra.pearl.core.service.workflow.RegistrationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class PreregistrationController implements PreregistrationApi {
  private ObjectMapper objectMapper;
  private RegistrationService registrationService;

  public PreregistrationController(
      ObjectMapper objectMapper, RegistrationService registrationService) {
    this.objectMapper = objectMapper;
    this.registrationService = registrationService;
  }

  @Override
  public ResponseEntity<Object> createAnonymous(
      String portalShortcode,
      String envName,
      String surveyStableId,
      Integer surveyVersion,
      Object body) {
    ParsedPreRegResponse response = objectMapper.convertValue(body, ParsedPreRegResponse.class);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    try {
      PreregistrationResponse createdResponse =
          registrationService.createAnonymousPreregistration(
              portalShortcode, environmentName, surveyStableId, surveyVersion, response);
      return ResponseEntity.ok(createdResponse);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("malformatted response data", e);
    }
  }

  /**
   * Confirms that a given preRegResponseId is valid to be used for a reigstration. This is
   * necessary for cases where the UI caches the registration ID across page refreshes, to ensure
   * the participant won't complete registration only to find out the linked preregistration was not
   * valid.
   */
  @Override
  public ResponseEntity<Object> confirm(
      String portalShortcode, String envName, UUID preRegResponseId) {
    Optional<PreregistrationResponse> responseOpt = registrationService.find(preRegResponseId);
    if (responseOpt.isPresent() && responseOpt.get().getPortalParticipantUserId() != null) {
      return ResponseEntity.unprocessableEntity().body("Already registered");
    }
    return ResponseEntity.of(responseOpt.map(response -> response));
  }
}
