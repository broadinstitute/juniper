package bio.terra.pearl.api.participant.service;

import bio.terra.pearl.core.dao.participant.ParticipantUserDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.ParticipantTaskService;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CurrentUserService {
  private ParticipantUserDao participantUserDao;
  private PortalParticipantUserService portalParticipantUserService;
  private EnrolleeService enrolleeService;
  private ParticipantTaskService participantTaskService;

  public CurrentUserService(
      ParticipantUserDao participantUserDao,
      PortalParticipantUserService portalParticipantUserService,
      EnrolleeService enrolleeService,
      ParticipantTaskService participantTaskService) {
    this.participantUserDao = participantUserDao;
    this.portalParticipantUserService = portalParticipantUserService;
    this.enrolleeService = enrolleeService;
    this.participantTaskService = participantTaskService;
  }

  @Transactional
  public Optional<UserWithEnrollees> unauthedLogin(
      String username, String portalShortcode, EnvironmentName environmentName) {
    Optional<ParticipantUser> userOpt = participantUserDao.findOne(username, environmentName);
    if (userOpt.isPresent()) {
      ParticipantUser user = userOpt.get();
      user = updateUserToken(user);
      return Optional.of(loadFromUser(user, portalShortcode));
    }
    return Optional.empty();
  }

  public Optional<UserWithEnrollees> tokenLogin(
      String token, String portalShortcode, EnvironmentName environmentName) {
    var decodedJWT = JWT.decode(token);
    var email = decodedJWT.getClaim("email").asString();
    var userOpt = participantUserDao.findOne(email, environmentName);
    if (userOpt.isPresent()) {
      var user = userOpt.get();
      return Optional.of(loadFromUser(user, portalShortcode));
    }
    return Optional.empty();
  }

  @Transactional
  public Optional<UserWithEnrollees> refresh(String token, String portalShortcode) {
    Optional<ParticipantUser> userOpt = participantUserDao.findByToken(token);
    if (userOpt.isPresent()) {
      ParticipantUser user = userOpt.get();
      user = updateUserToken(user);
      return Optional.of(loadFromUser(user, portalShortcode));
    }
    return Optional.empty();
  }

  protected ParticipantUser updateUserToken(ParticipantUser user) {
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

  public UserWithEnrollees loadFromUser(ParticipantUser user, String portalShortcode) {
    // this get() will fail if the user has not registered for the given portal, which is what we
    // want
    PortalParticipantUser portalParticipantUser =
        portalParticipantUserService.findOne(user.getId(), portalShortcode).get();
    user.getPortalParticipantUsers().add(portalParticipantUser);
    List<Enrollee> enrollees = enrolleeService.findByPortalParticipantUser(portalParticipantUser);
    for (Enrollee enrollee : enrollees) {
      enrollee
          .getParticipantTasks()
          .addAll(participantTaskService.findByEnrolleeId(enrollee.getId()));
    }
    return new UserWithEnrollees(user, enrollees);
  }

  public record UserWithEnrollees(ParticipantUser user, List<Enrollee> enrollees) {}

  @Transactional
  public void logout(String token) {
    Optional<ParticipantUser> userOpt = participantUserDao.findByToken(token);
    ParticipantUser user = userOpt.get();
    user.setToken(null);
    participantUserDao.update(user);
  }

  public Optional<ParticipantUser> findByToken(String token) {
    if (token == null) {
      return Optional.empty();
    }
    return participantUserDao.findByToken(token);
  }

  public Optional<ParticipantUser> findByUsername(String username) {
    // TODO: paramaterize environment
    return participantUserDao.findOne(username, EnvironmentName.sandbox);
  }
}
