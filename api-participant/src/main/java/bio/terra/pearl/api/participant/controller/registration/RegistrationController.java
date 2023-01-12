package bio.terra.pearl.api.participant.controller.registration;

import bio.terra.pearl.api.participant.api.RegistrationApi;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.survey.ParsedSnapshot;
import bio.terra.pearl.core.service.RegistrationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class RegistrationController implements RegistrationApi {
  private ObjectMapper objectMapper;
  private RegistrationService registrationService;

  public RegistrationController(
      ObjectMapper objectMapper, RegistrationService registrationService) {
    this.objectMapper = objectMapper;
    this.registrationService = registrationService;
  }

  @Override
  public ResponseEntity<Object> register(
      String portalShortcode, String envName, UUID preRegResponseId, Object body) {
    ParsedSnapshot response = objectMapper.convertValue(body, ParsedSnapshot.class);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);

    ParticipantUser user =
        registrationService.register(portalShortcode, environmentName, response, preRegResponseId);
    return ResponseEntity.ok(user);
  }
}
