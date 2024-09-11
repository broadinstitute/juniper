package bio.terra.pearl.api.admin.service;

import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.admin.AdminUserWithPermissions;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.service.admin.AdminUserService;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.time.Instant;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {
  private AdminUserService adminUserService;

  public CurrentUserService(AdminUserService adminUserService) {
    this.adminUserService = adminUserService;
  }

  public Optional<AdminUserWithPermissions> tokenLogin(String token) {
    Optional<AdminUserWithPermissions> userWithPermsOpt = loadByToken(token);
    userWithPermsOpt.ifPresent(
        userWithPerms -> {
          AdminUser user = userWithPerms.user();
          user.setLastLogin(Instant.now());
          adminUserService.update(
              user, DataAuditInfo.builder().responsibleAdminUserId(user.getId()).build());
        });
    return userWithPermsOpt;
  }

  public Optional<AdminUserWithPermissions> refresh(String token) {
    return loadByToken(token);
  }

  protected Optional<AdminUserWithPermissions> loadByToken(String token) {
    DecodedJWT decodedJWT = JWT.decode(token);
    String email = decodedJWT.getClaim("email").asString();
    return adminUserService.findByUsernameWithPermissions(email);
  }

  public void logout(String token) {
    // no-op
  }
}
