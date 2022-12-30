package bio.terra.pearl.api.participant.controller.registration;

import bio.terra.pearl.api.participant.api.PreregistrationApi;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.survey.ParsedPreRegResponse;
import bio.terra.pearl.core.model.survey.PreregistrationResponse;
import bio.terra.pearl.core.service.RegistrationService;
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
      String studyShortcode,
      String surveyStableId,
      Integer surveyVersion,
      Object body) {
    ParsedPreRegResponse response = objectMapper.convertValue(body, ParsedPreRegResponse.class);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    try {
      String responseData = objectMapper.writeValueAsString(response.getParsedData());
      PreregistrationResponse createdResponse =
          registrationService.createAnonymousPreregistration(
              portalShortcode,
              environmentName,
              studyShortcode,
              surveyStableId,
              surveyVersion,
              responseData);
      return ResponseEntity.ok(createdResponse);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("malformatted response data", e);
    }
  }

  @Override
  public ResponseEntity<Object> confirm(
      String portalShortcode, String envName, String studyShortcode, UUID preRegResponseId) {
    Optional<PreregistrationResponse> responseOpt = registrationService.find(preRegResponseId);
    return ResponseEntity.of(responseOpt.map(response -> response));
  }
}
