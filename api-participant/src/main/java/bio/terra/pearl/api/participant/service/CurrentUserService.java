package bio.terra.pearl.api.participant.service;

import bio.terra.pearl.core.dao.participant.ParticipantUserDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CurrentUserService {
  private ParticipantUserDao participantUserDao;

  public CurrentUserService(ParticipantUserDao participantUserDao) {
    this.participantUserDao = participantUserDao;
  }

  @Transactional
  public Optional<ParticipantUser> unauthedLogin(String username, EnvironmentName environmentName) {
    Optional<ParticipantUser> userOpt = participantUserDao.findOne(username, environmentName);
    userOpt.ifPresent(
        user -> {
          UUID token = UUID.randomUUID();
          user.setToken(token.toString());
          user.setLastLogin(Instant.now());
          participantUserDao.update(user);
        });
    return userOpt;
  }

  @Transactional
  public Optional<ParticipantUser> refresh(String token) {
    Optional<ParticipantUser> userOpt = participantUserDao.findByToken(token);
    userOpt.ifPresent(
        user -> {
          UUID newToken = UUID.randomUUID();
          user.setToken(newToken.toString());
          user.setLastLogin(Instant.now());
          participantUserDao.update(user);
        });

    return userOpt;
  }

  @Transactional
  public void logout(String token) {
    Optional<ParticipantUser> userOpt = participantUserDao.findByToken(token);
    ParticipantUser user = userOpt.get();
    user.setToken(null);
    participantUserDao.update(user);
  }

  public Optional<ParticipantUser> findByToken(String token) {
    return participantUserDao.findByToken(token);
  }
}
