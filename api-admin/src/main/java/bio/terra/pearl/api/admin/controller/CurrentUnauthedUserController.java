package bio.terra.pearl.api.admin.controller;

import bio.terra.pearl.api.admin.api.CurrentUnauthedUserApi;
import bio.terra.pearl.api.admin.model.TokenLoginInfo;
import bio.terra.pearl.api.admin.service.CurrentUnauthedUserService;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

/**
 * quick hack controller to allow fake logins to admin tool. This will likely become obsolete with
 * B2C
 */
@Controller
public class CurrentUnauthedUserController implements CurrentUnauthedUserApi {
  private CurrentUnauthedUserService currentUnauthedUserService;

  public CurrentUnauthedUserController(CurrentUnauthedUserService currentUnauthedUserService) {
    this.currentUnauthedUserService = currentUnauthedUserService;
  }

  @Override
  public ResponseEntity<Object> unauthedLogin(String username) {
    // for now, log them in as long as the username exists
    Optional<CurrentUnauthedUserService.AdminUserWithPermissionsAndToken> adminUserOpt =
        currentUnauthedUserService.unauthedLogin(username);
    return ResponseEntity.of(adminUserOpt.map(adminUser -> adminUser));
  }

  @Override
  public ResponseEntity<Object> refresh(TokenLoginInfo tokenInfo) {
    // for now, log them in as long as the username exists
    Optional<CurrentUnauthedUserService.AdminUserWithPermissionsAndToken> adminUserOpt =
        currentUnauthedUserService.tokenLogin(tokenInfo.getToken());
    return ResponseEntity.of(adminUserOpt.map(adminUser -> adminUser));
  }
}
