package bio.terra.pearl.api.admin.controller;

import bio.terra.pearl.api.admin.api.CurrentUserApi;
import bio.terra.pearl.api.admin.model.TokenLoginInfo;
import bio.terra.pearl.api.admin.service.CurrentUserService;
import bio.terra.pearl.core.model.admin.AdminUser;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

/**
 * quick hack controller to allow fake logins to admin tool. This will likely become obsolete with
 * B2C
 */
@Controller
public class CurrentUserController implements CurrentUserApi {
  private CurrentUserService currentUserService;

  public CurrentUserController(CurrentUserService currentUserService) {
    this.currentUserService = currentUserService;
  }

  @Override
  public ResponseEntity<Object> unauthedLogin(String username) {
    // for now, log them in as long as the username exists
    Optional<AdminUser> adminUserOpt = currentUserService.unauthedLogin(username);
    return ResponseEntity.of(adminUserOpt.map(adminUser -> adminUser));
  }

  @Override
  public ResponseEntity<Object> tokenLogin(TokenLoginInfo tokenInfo) {
    // for now, log them in as long as the username exists
    Optional<AdminUser> adminUserOpt = currentUserService.tokenLogin(tokenInfo.getToken());
    return ResponseEntity.of(adminUserOpt.map(adminUser -> adminUser));
  }

  public record AdminUserDto(String username) {}
}
