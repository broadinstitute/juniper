package bio.terra.pearl.api.participant.controller.registration;

import bio.terra.pearl.api.participant.api.RegistrationApi;
import bio.terra.pearl.api.participant.model.RegistrationInfo;
import bio.terra.pearl.api.participant.service.CurrentUnauthedUserService;
import bio.terra.pearl.api.participant.service.CurrentUserService;
import bio.terra.pearl.api.participant.service.RequestUtilService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.service.workflow.RegistrationService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class RegistrationController implements RegistrationApi {
  private final CurrentUserService currentUserService;
  private final CurrentUnauthedUserService currentUnauthedUserService;
  private final HttpServletRequest request;
  private final RegistrationService registrationService;
  private final RequestUtilService requestUtilService;

  public RegistrationController(
      CurrentUserService currentUserService,
      CurrentUnauthedUserService currentUnauthedUserService,
      HttpServletRequest request,
      RegistrationService registrationService,
      RequestUtilService requestUtilService) {
    this.currentUserService = currentUserService;
    this.currentUnauthedUserService = currentUnauthedUserService;
    this.request = request;
    this.registrationService = registrationService;
    this.requestUtilService = requestUtilService;
  }

  @Override
  public ResponseEntity<Object> register(
      String portalShortcode,
      String envName,
      String preferredLanguage,
      UUID preRegResponseId,
      RegistrationInfo body) {
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    String token = requestUtilService.requireToken(request);
    registrationService.register(
        portalShortcode, environmentName, body.getEmail(), preRegResponseId, preferredLanguage);
    CurrentUserService.UserLoginDto userLoginDto =
        currentUserService.tokenLogin(token, portalShortcode, environmentName);
    return ResponseEntity.ok(userLoginDto);
  }

  @Override
  public ResponseEntity<Object> registerOrLogin(
      String portalShortcode,
      String envName,
      String preferredLanguage,
      UUID preRegResponseId,
      RegistrationInfo body) {
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    String token = requestUtilService.requireToken(request);
    CurrentUserService.UserLoginDto userLoginDto =
        currentUserService.registerOrLogin(
            token, portalShortcode, environmentName, preRegResponseId, preferredLanguage);
    return ResponseEntity.ok(userLoginDto);
  }

  @Override
  public ResponseEntity<Object> internalRegister(
      String portalShortcode,
      String envName,
      String preferredLanguage,
      UUID preRegResponseId,
      RegistrationInfo body) {
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);

    RegistrationService.RegistrationResult registrationResult =
        registrationService.register(
            portalShortcode, environmentName, body.getEmail(), preRegResponseId, preferredLanguage);
    CurrentUserService.UserLoginDto loggedInUser =
        currentUnauthedUserService.unauthedLogin(
            registrationResult.participantUser().getUsername(), portalShortcode, environmentName);
    return ResponseEntity.ok(loggedInUser);
  }
}
