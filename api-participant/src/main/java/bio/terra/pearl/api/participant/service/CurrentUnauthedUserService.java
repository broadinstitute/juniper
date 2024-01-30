package bio.terra.pearl.api.participant.service;

import bio.terra.common.exception.UnauthorizedException;
import bio.terra.pearl.core.dao.participant.ParticipantUserDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
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
  public CurrentUserService.UserWithEnrollees unauthedRefresh(
      String token, String portalShortcode, EnvironmentName environmentName) {
    return currentUserService.refresh(token, portalShortcode, environmentName);
  }

  @Transactional
  public CurrentUserService.UserWithEnrollees unauthedLogin(
      String username, String portalShortcode, EnvironmentName environmentName) {
    Optional<ParticipantUser> userOpt = participantUserDao.findOne(username, environmentName);
    if (userOpt.isEmpty()) {
      log.info("User not found for environment {}. (Portal: {})", environmentName, portalShortcode);
      throw new UnauthorizedException("User not found for environment " + environmentName);
    }
    ParticipantUser user = userOpt.get();
    user = updateUnauthedUserToken(user);
    return currentUserService.loadFromUser(user, portalShortcode);
  }

  protected ParticipantUser updateUnauthedUserToken(ParticipantUser user) {
    String newToken = generateFakeJwtToken(user.getUsername());
    user.setToken(newToken);
    user.setLastLogin(Instant.now());
    return participantUserDao.update(user);
  }

  String generateFakeJwtToken(String username) {
    UUID token = UUID.randomUUID();
    return JWT.create()
        .withClaim("token", token.toString())
        .withClaim("email", username)
        .sign(Algorithm.none());
  }
}
