package bio.terra.pearl.api.admin.service;

import bio.terra.pearl.core.dao.admin.AdminUserDao;
import bio.terra.pearl.core.model.admin.AdminUser;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CurrentUserService {
  private AdminUserDao adminUserDao;

  public CurrentUserService(AdminUserDao adminUserDao) {
    this.adminUserDao = adminUserDao;
  }

  @Transactional
  public Optional<AdminUser> unauthedLogin(String username) {
    Optional<AdminUser> userOpt = adminUserDao.findByUsername(username);
    userOpt.ifPresent(
        user -> {
          UUID token = UUID.randomUUID();
          user.setToken(token.toString());
          adminUserDao.update(user);
        });
    return userOpt;
  }

  @Transactional
  public Optional<AdminUser> tokenLogin(String token) {
    Optional<AdminUser> userOpt = adminUserDao.findByToken(token);
    userOpt.ifPresent(
        user -> {
          UUID newToken = UUID.randomUUID();
          user.setToken(newToken.toString());
          adminUserDao.update(user);
        });
    return userOpt;
  }

  @Transactional
  public void logout(String token) {
    Optional<AdminUser> userOpt = adminUserDao.findByToken(token);
    userOpt.ifPresent(
        user -> {
          user.setToken(null);
          adminUserDao.update(user);
        });
  }

  public Optional<AdminUser> findByToken(String token) {
    return adminUserDao.findByToken(token);
  }
}
