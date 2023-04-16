package bio.terra.pearl.api.participant.controller.registration;

import bio.terra.pearl.api.participant.api.RegistrationApi;
import bio.terra.pearl.api.participant.model.RegistrationInfo;
import bio.terra.pearl.api.participant.service.CurrentUnauthedUserService;
import bio.terra.pearl.api.participant.service.CurrentUserService;
import bio.terra.pearl.api.participant.service.RequestUtilService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.service.workflow.RegistrationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import java.util.function.Function;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class RegistrationController implements RegistrationApi {
  private CurrentUserService currentUserService;
  private CurrentUnauthedUserService currentUnauthedUserService;
  private HttpServletRequest request;
  private ObjectMapper objectMapper;
  private RegistrationService registrationService;
  private RequestUtilService requestUtilService;

  public RegistrationController(
      CurrentUserService currentUserService,
      CurrentUnauthedUserService currentUnauthedUserService,
      HttpServletRequest request,
      ObjectMapper objectMapper,
      RegistrationService registrationService,
      RequestUtilService requestUtilService) {
    this.currentUserService = currentUserService;
    this.currentUnauthedUserService = currentUnauthedUserService;
    this.request = request;
    this.objectMapper = objectMapper;
    this.registrationService = registrationService;
    this.requestUtilService = requestUtilService;
  }

  @Override
  public ResponseEntity<Object> register(
      String portalShortcode, String envName, UUID preRegResponseId, RegistrationInfo body) {
    var environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    registrationService.register(
        portalShortcode, environmentName, body.getEmail(), preRegResponseId);
    var token = requestUtilService.requireToken(request);
    var userWithEnrollees = currentUserService.tokenLogin(token, portalShortcode, environmentName);
    return ResponseEntity.of(userWithEnrollees.map(Function.identity()));
  }

  @Override
  public ResponseEntity<Object> internalRegister(
      String portalShortcode, String envName, UUID preRegResponseId, RegistrationInfo body) {
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);

    var registrationResult =
        registrationService.register(
            portalShortcode, environmentName, body.getEmail(), preRegResponseId);
    // log in the user if not already
    if (registrationResult.participantUser().getToken() == null) {
      CurrentUserService.UserWithEnrollees loggedInUser =
          currentUnauthedUserService
              .unauthedLogin(
                  registrationResult.participantUser().getUsername(),
                  portalShortcode,
                  environmentName)
              .get();
      registrationResult =
          new RegistrationService.RegistrationResult(
              loggedInUser.user(), registrationResult.portalParticipantUser());
    }
    return ResponseEntity.ok(registrationResult);
  }
}
