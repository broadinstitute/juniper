package bio.terra.pearl.api.participant.controller.registration;

import bio.terra.pearl.api.participant.api.PreregistrationApi;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.survey.PreregistrationResponse;
import bio.terra.pearl.core.service.RegistrationService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    PreregistrationResponse response =
        objectMapper.convertValue(body, PreregistrationResponse.class);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    PreregistrationResponse createdResponse =
        registrationService.createAnonymousPreregistration(
            portalShortcode,
            environmentName,
            studyShortcode,
            surveyStableId,
            surveyVersion,
            response.getFullData());
    return ResponseEntity.ok(createdResponse);
  }
}
