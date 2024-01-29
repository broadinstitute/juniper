package bio.terra.pearl.api.admin.service;

import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.admin.AdminUserWithPermissions;
import bio.terra.pearl.core.service.admin.AdminUserService;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class CurrentUnauthedUserService {
  private AdminUserService adminUserService;

  public CurrentUnauthedUserService(AdminUserService adminUserService) {
    this.adminUserService = adminUserService;
  }

  public Optional<AdminUserWithPermissions> unauthedLogin(String username) {
    Optional<AdminUserWithPermissions> userOpt =
        adminUserService.findByUsernameWithPermissions(username);
    userOpt.ifPresent(
        userWithPermissions -> {
          AdminUser user = userWithPermissions.user();
          user.setToken(generateFakeJwtToken(username));
          user.setLastLogin(Instant.now());
          adminUserService.update(user);
        });
    return userOpt;
  }

  protected String generateFakeJwtToken(String username) {
    UUID token = UUID.randomUUID();
    return JWT.create()
        .withClaim("token", token.toString())
        .withClaim("email", username)
        .sign(Algorithm.none());
  }

  public Optional<AdminUserWithPermissions> tokenLogin(String token) {
    String email = getEmailFromToken(token);
    return adminUserService.findByUsernameWithPermissions(email);
  }

  public void logout(String token) {
    String email = getEmailFromToken(token);
    Optional<AdminUser> userOpt = adminUserService.findByUsername(email);
    userOpt.ifPresent(
        user -> {
          user.setToken(null);
          adminUserService.update(user);
        });
  }

  protected String getEmailFromToken(String token) {
    DecodedJWT decodedJWT = JWT.decode(token);
    return decodedJWT.getClaim("email").asString();
  }
}
