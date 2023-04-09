package bio.terra.pearl.api.admin.controller;

import bio.terra.pearl.api.admin.api.CurrentUserApi;
import bio.terra.pearl.api.admin.model.TokenLoginInfo;
import bio.terra.pearl.api.admin.service.CurrentUserService;
import bio.terra.pearl.core.model.admin.AdminUserWithPermissions;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class CurrentUserController implements CurrentUserApi {
  private CurrentUserService currentUserService;

  public CurrentUserController(CurrentUserService currentUserService) {
    this.currentUserService = currentUserService;
  }

  @Override
  public ResponseEntity<Object> login(TokenLoginInfo tokenInfo) {
    // for now, log them in as long as the username exists
    Optional<AdminUserWithPermissions> adminUserOpt =
        currentUserService.tokenLogin(tokenInfo.getToken());
    return ResponseEntity.of(adminUserOpt.map(adminUser -> adminUser));
  }

  @Override
  public ResponseEntity<Object> refresh(TokenLoginInfo tokenInfo) {
    // for now, log them in as long as the username exists
    Optional<AdminUserWithPermissions> adminUserOpt =
        currentUserService.refresh(tokenInfo.getToken());
    return ResponseEntity.of(adminUserOpt.map(adminUser -> adminUser));
  }
}
