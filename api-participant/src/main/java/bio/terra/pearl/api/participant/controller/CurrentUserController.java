package bio.terra.pearl.api.participant.controller;

import bio.terra.pearl.api.participant.api.CurrentUserApi;
import bio.terra.pearl.api.participant.service.CurrentUserService;
import bio.terra.pearl.api.participant.service.RequestUtilService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class CurrentUserController implements CurrentUserApi {
  private CurrentUserService currentUserService;
  private HttpServletRequest request;
  private RequestUtilService requestUtilService;

  public CurrentUserController(
      CurrentUserService currentUserService,
      HttpServletRequest request,
      RequestUtilService requestUtilService) {
    this.currentUserService = currentUserService;
    this.request = request;
    this.requestUtilService = requestUtilService;
  }

  @Override
  public ResponseEntity<Object> tokenLogin(String portalShortcode, String envName) {
    String token = requestUtilService.requireToken(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    CurrentUserService.UserWithEnrollees userWithEnrollees =
        currentUserService.tokenLogin(token, portalShortcode, environmentName);
    return ResponseEntity.ok(userWithEnrollees);
  }

  @Override
  public ResponseEntity<Object> refresh(String portalShortcode, String envName) {
    String token = requestUtilService.requireToken(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    CurrentUserService.UserWithEnrollees userOpt =
        currentUserService.refresh(token, portalShortcode, environmentName);
    return ResponseEntity.ok(userOpt);
  }

  @Override
  public ResponseEntity<Void> logout(String portalShortcode, String envName) {
    /**
     * this currently does a global logout. That may change as we determine how portals interact
     * with each other and how we whitelabel.
     */
    ParticipantUser user = requestUtilService.requireUser(request);
    currentUserService.logout(user);
    return ResponseEntity.noContent().build();
  }
}
