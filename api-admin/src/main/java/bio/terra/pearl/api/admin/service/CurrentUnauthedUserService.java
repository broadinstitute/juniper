package bio.terra.pearl.api.admin.service;

import bio.terra.common.exception.NotFoundException;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.admin.AdminUserWithPermissions;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.service.admin.AdminUserService;
import bio.terra.pearl.core.shared.ApplicationRoutingPaths;
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
  private ApplicationRoutingPaths applicationRoutingPaths;

  public CurrentUnauthedUserService(
      AdminUserService adminUserService, ApplicationRoutingPaths applicationRoutingPaths) {
    this.adminUserService = adminUserService;
    this.applicationRoutingPaths = applicationRoutingPaths;
  }

  public Optional<AdminUserWithPermissionsAndToken> unauthedLogin(String username) {
    requireUnauthedLoginAllowed();
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

  /**
   * These routes trust unsigned tokens and log in by username alone, so they must never be
   * reachable in a deployed environment. This mirrors the UI, which only offers developer login on
   * localhost. Responds as not found so the route's existence isn't advertised.
   */
  protected void requireUnauthedLoginAllowed() {
    if (!applicationRoutingPaths.getDeploymentZone().equalsIgnoreCase("local")) {
      throw new NotFoundException("Not found");
    }
  }

  protected String generateFakeJwtToken(String username) {
    UUID token = UUID.randomUUID();
    return JWT.create()
        .withClaim("token", token.toString())
        .withClaim("email", username)
        .sign(Algorithm.none());
  }

  public Optional<AdminUserWithPermissionsAndToken> tokenLogin(String token) {
    requireUnauthedLoginAllowed();
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
