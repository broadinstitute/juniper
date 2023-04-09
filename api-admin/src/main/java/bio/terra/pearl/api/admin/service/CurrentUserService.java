package bio.terra.pearl.api.admin.service;

import bio.terra.pearl.core.dao.admin.AdminUserDao;
import bio.terra.pearl.core.model.admin.AdminUserWithPermissions;
import com.auth0.jwt.JWT;
import java.time.Instant;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CurrentUserService {
  private AdminUserDao adminUserDao;

  public CurrentUserService(AdminUserDao adminUserDao) {
    this.adminUserDao = adminUserDao;
  }

  public Optional<AdminUserWithPermissions> tokenLogin(String token) {
    Optional<AdminUserWithPermissions> userWithPermsOpt = loadByToken(token);
    userWithPermsOpt.ifPresent(
        userWithPerms -> {
          userWithPerms.user().setLastLogin(Instant.now());
          adminUserDao.update(userWithPerms.user());
        });
    return userWithPermsOpt;
  }

  @Transactional
  public Optional<AdminUserWithPermissions> refresh(String token) {
    return loadByToken(token);
  }

  protected Optional<AdminUserWithPermissions> loadByToken(String token) {
    var decodedJWT = JWT.decode(token);
    var email = decodedJWT.getClaim("email").asString();
    return adminUserDao.findByUsernameWithPermissions(email);
  }

  public void logout(String token) {
    // no-op
  }
}
