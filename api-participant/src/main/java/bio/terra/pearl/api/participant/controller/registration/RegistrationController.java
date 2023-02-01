package bio.terra.pearl.api.participant.controller.registration;

import bio.terra.pearl.api.participant.api.RegistrationApi;
import bio.terra.pearl.api.participant.service.CurrentUserService;
import bio.terra.pearl.core.model.EnvironmentName;
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
  private CurrentUserService currentUserService;

  public RegistrationController(
      ObjectMapper objectMapper,
      RegistrationService registrationService,
      CurrentUserService currentUserService) {
    this.objectMapper = objectMapper;
    this.registrationService = registrationService;
    this.currentUserService = currentUserService;
  }

  @Override
  public ResponseEntity<Object> register(
      String portalShortcode, String envName, UUID preRegResponseId, Object body) {
    ParsedSnapshot response = objectMapper.convertValue(body, ParsedSnapshot.class);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);

    RegistrationService.RegistrationResult registrationResult =
        registrationService.register(portalShortcode, environmentName, response, preRegResponseId);
    // log in the user if not already
    if (registrationResult.participantUser().getToken() == null) {
      CurrentUserService.UserWithEnrollees loggedInUser =
          currentUserService
              .unauthedLogin(registrationResult.participantUser().getUsername(), environmentName)
              .get();
      registrationResult =
          new RegistrationService.RegistrationResult(
              loggedInUser.user(), registrationResult.portalParticipantUser());
    }
    return ResponseEntity.ok(registrationResult);
  }
}
