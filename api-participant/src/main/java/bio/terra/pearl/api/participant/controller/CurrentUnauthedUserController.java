package bio.terra.pearl.api.participant.controller;

import bio.terra.pearl.api.participant.api.CurrentUnauthedUserApi;
import bio.terra.pearl.api.participant.service.CurrentUnauthedUserService;
import bio.terra.pearl.api.participant.service.CurrentUserService;
import bio.terra.pearl.api.participant.service.RequestUtilService;
import bio.terra.pearl.core.model.EnvironmentName;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

/**
 * these routes live under /api/public, so the Apache proxy does NOT block them. The service layer
 * disables them outside the "local" deployment zone -- see
 * CurrentUnauthedUserService.requireUnauthedLoginAllowed. The tokens produced are unsigned and
 * would not get past the proxy on authenticated routes.
 */
@Controller
public class CurrentUnauthedUserController implements CurrentUnauthedUserApi {
  private CurrentUnauthedUserService unauthedUserService;
  private RequestUtilService requestUtilService;
  private HttpServletRequest request;

  public CurrentUnauthedUserController(
      CurrentUnauthedUserService currentUserService,
      RequestUtilService requestUtilService,
      HttpServletRequest httpServletRequest) {
    this.unauthedUserService = currentUserService;
    this.requestUtilService = requestUtilService;
    this.request = httpServletRequest;
  }

  @Override
  public ResponseEntity<Object> unauthedLogin(
      String portalShortcode, String envName, String username) {
    /**
     * this currently does a global login. That may change as we determine how portals interact with
     * each other and how we whitelabel.
     */
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    // for now, log them in as long as the username exists
    CurrentUserService.UserLoginDto userOpt =
        unauthedUserService.unauthedLogin(username, portalShortcode, environmentName);
    return ResponseEntity.ok(userOpt);
  }

  @Override
  public ResponseEntity<Object> unauthedRefresh(String portalShortcode, String envName) {
    /**
     * this currently does a global login. That may change as we determine how portals interact with
     * each other and how we whitelabel.
     */
    String token = requestUtilService.requireToken(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    // for now, log them in as long as the username exists
    CurrentUserService.UserLoginDto userOpt =
        unauthedUserService.unauthedRefresh(token, portalShortcode, environmentName);
    return ResponseEntity.ok(userOpt);
  }
}
