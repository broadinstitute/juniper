package bio.terra.pearl.api.participant.service;

import bio.terra.common.exception.NotFoundException;
import bio.terra.common.exception.UnauthorizedException;
import bio.terra.pearl.core.dao.participant.ParticipantUserDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.shared.ApplicationRoutingPaths;
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

  private ApplicationRoutingPaths applicationRoutingPaths;

  public CurrentUnauthedUserService(
      ParticipantUserDao participantUserDao,
      CurrentUserService currentUserService,
      ApplicationRoutingPaths applicationRoutingPaths) {
    this.participantUserDao = participantUserDao;
    this.currentUserService = currentUserService;
    this.applicationRoutingPaths = applicationRoutingPaths;
  }

  @Transactional
  public CurrentUserService.UserLoginDto unauthedRefresh(
      String token, String portalShortcode, EnvironmentName environmentName) {
    requireUnauthedLoginAllowed();
    return currentUserService.refresh(token, portalShortcode, environmentName);
  }

  @Transactional
  public CurrentUserService.UserLoginDto unauthedLogin(
      String username, String portalShortcode, EnvironmentName environmentName) {
    requireUnauthedLoginAllowed();
    Optional<ParticipantUser> userOpt = participantUserDao.findOne(username, environmentName);
    if (userOpt.isEmpty()) {
      log.info("User not found for environment {}. (Portal: {})", environmentName, portalShortcode);
      throw new UnauthorizedException("User not found for environment " + environmentName);
    }
    ParticipantUser user = userOpt.get();
    user = updateUnauthedUserToken(user);
    return currentUserService.loadFromUser(user, portalShortcode);
  }

  /**
   * These routes are unauthenticated and trust unsigned tokens, so they must never be reachable in
   * a deployed environment. This mirrors the UI, which only offers unauthed login in bundles built
   * with VITE_UNAUTHED_LOGIN (local dev and CI e2e runs, both of which use the "local" deployment
   * zone). Responds as not found so the route's existence isn't advertised.
   */
  public void requireUnauthedLoginAllowed() {
    if (!applicationRoutingPaths.getDeploymentZone().equalsIgnoreCase("local")) {
      throw new NotFoundException("Not found");
    }
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
