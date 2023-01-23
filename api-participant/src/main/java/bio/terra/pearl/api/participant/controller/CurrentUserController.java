package bio.terra.pearl.api.participant.controller;

import bio.terra.pearl.api.participant.api.CurrentUserApi;
import bio.terra.pearl.api.participant.service.CurrentUserService;
import bio.terra.pearl.api.participant.service.RequestUtilService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
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
  public ResponseEntity<Object> unauthedLogin(
      String portalShortcode, String envName, String username) {
    /**
     * this currently does a global login. That may change as we determine how portals interact with
     * each other and how we whitelabel.
     */
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    // for now, log them in as long as the username exists
    Optional<ParticipantUser> adminUserOpt =
        currentUserService.unauthedLogin(username, environmentName);
    return ResponseEntity.of(adminUserOpt.map(adminUser -> adminUser));
  }

  @Override
  public ResponseEntity<Object> refresh(String portalShortcode, String envName) {
    String token = requestUtilService.tokenFromRequest(request);
    Optional<ParticipantUser> userOpt = currentUserService.refresh(token);
    return ResponseEntity.of(userOpt.map(user -> user));
  }

  @Override
  public ResponseEntity<Void> logout(String portalShortcode, String envName) {
    /**
     * this currently does a global logout. That may change as we determine how portals interact
     * with each other and how we whitelabel.
     */
    String token = requestUtilService.tokenFromRequest(request);
    currentUserService.logout(token);
    return ResponseEntity.noContent().build();
  }
}
