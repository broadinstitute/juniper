package bio.terra.pearl.api.participant.service;

import bio.terra.pearl.core.dao.participant.ParticipantUserDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CurrentUnauthedUserService {
  private ParticipantUserDao participantUserDao;

  private CurrentUserService currentUserService;

  public CurrentUnauthedUserService(
      ParticipantUserDao participantUserDao, CurrentUserService currentUserService) {
    this.participantUserDao = participantUserDao;
    this.currentUserService = currentUserService;
  }

  @Transactional
  public Optional<CurrentUserService.UserWithEnrollees> unauthedRefresh(
      String token, String portalShortcode, EnvironmentName environmentName) {
    return currentUserService.refresh(token, portalShortcode, environmentName);
  }

  @Transactional
  public Optional<CurrentUserService.UserWithEnrollees> unauthedLogin(
      String username, String portalShortcode, EnvironmentName environmentName) {
    Optional<ParticipantUser> userOpt = participantUserDao.findOne(username, environmentName);
    if (userOpt.isPresent()) {
      ParticipantUser user = userOpt.get();
      user = updateUnauthedUserToken(user);
      return Optional.of(currentUserService.loadFromUser(user, portalShortcode));
    }
    return Optional.empty();
  }

  protected ParticipantUser updateUnauthedUserToken(ParticipantUser user) {
    var newToken = generateFakeJwtToken(user.getUsername());
    user.setToken(newToken);
    user.setLastLogin(Instant.now());
    return participantUserDao.update(user);
  }

  String generateFakeJwtToken(String username) {
    var token = UUID.randomUUID();
    return JWT.create()
        .withClaim("token", token.toString())
        .withClaim("email", username)
        .sign(Algorithm.none());
  }
}
