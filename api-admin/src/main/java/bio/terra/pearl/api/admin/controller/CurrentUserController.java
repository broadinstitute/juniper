package bio.terra.pearl.api.admin.controller;

import bio.terra.pearl.api.admin.api.CurrentUserApi;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.service.admin.AdminUserService;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

/**
 * quick hack controller to allow fake logins to admin tool. This will likely become obsolete with
 * B2C
 */
@Controller
public class CurrentUserController implements CurrentUserApi {
  private AdminUserService adminUserService;

  public CurrentUserController(AdminUserService adminUserService) {
    this.adminUserService = adminUserService;
  }

  public ResponseEntity<Object> unauthedLogin(String username) {
    // for now, log them in as long as the username exists
    Optional<AdminUser> adminUserOpt = adminUserService.findByUsername(username);
    return ResponseEntity.of(adminUserOpt.map(adminUser -> adminUser));
  }

  public record AdminUserDto(String username) {}
}
