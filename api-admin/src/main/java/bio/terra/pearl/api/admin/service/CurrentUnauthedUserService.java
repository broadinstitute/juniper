package bio.terra.pearl.api.admin.service;

import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.admin.AdminUserWithPermissions;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.service.admin.AdminUserService;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class CurrentUnauthedUserService {
  private AdminUserService adminUserService;

  public CurrentUnauthedUserService(AdminUserService adminUserService) {
    this.adminUserService = adminUserService;
  }

  public Optional<AdminUserWithPermissionsAndToken> unauthedLogin(String username) {
    Optional<AdminUserWithPermissions> userOpt =
        adminUserService.findByUsernameWithPermissions(username);
    if (userOpt.isPresent()) {
      AdminUser user = userOpt.get().user();
      user.setLastLogin(Instant.now());
      adminUserService.update(
          user, DataAuditInfo.builder().responsibleAdminUserId(user.getId()).build());
      return Optional.of(
          new AdminUserWithPermissionsAndToken(
              user, userOpt.get().portalPermissions(), generateFakeJwtToken(username)));
    }
    return Optional.empty();
  }

  protected String generateFakeJwtToken(String username) {
    UUID token = UUID.randomUUID();
    return JWT.create()
        .withClaim("token", token.toString())
        .withClaim("email", username)
        .sign(Algorithm.none());
  }

  public Optional<AdminUserWithPermissionsAndToken> tokenLogin(String token) {
    String email = getEmailFromToken(token);

    Optional<AdminUserWithPermissions> userOpt =
        adminUserService.findByUsernameWithPermissions(email);
    if (userOpt.isPresent()) {
      return Optional.of(
          new AdminUserWithPermissionsAndToken(
              userOpt.get().user(),
              userOpt.get().portalPermissions(),
              generateFakeJwtToken(email)));
    }
    return Optional.empty();
  }

  public void logout(String token) {
    String email = getEmailFromToken(token);
    Optional<AdminUser> userOpt = adminUserService.findByUsername(email);
    // no-op
  }

  protected String getEmailFromToken(String token) {
    DecodedJWT decodedJWT = JWT.decode(token);
    return decodedJWT.getClaim("email").asString();
  }

  public record AdminUserWithPermissionsAndToken(
      AdminUser user, Map<UUID, HashSet<String>> portalPermissions, String token) {}
}
