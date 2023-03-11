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
import java.time.Instant;
import java.util.List;
import java.util.Optional;
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

  public Optional<UserWithEnrollees> tokenLogin(
      String token, String portalShortcode, EnvironmentName environmentName) {
    Optional<UserWithEnrollees> userOpt = loadByToken(token, portalShortcode, environmentName);
    userOpt.ifPresent(
        userAndEnrolles -> {
          userAndEnrolles.user.setLastLogin(Instant.now());
          participantUserDao.update(userAndEnrolles.user);
        });
    return userOpt;
  }

  @Transactional
  public Optional<UserWithEnrollees> refresh(
      String token, String portalShortcode, EnvironmentName environmentName) {
    return loadByToken(token, portalShortcode, environmentName);
  }

  protected Optional<UserWithEnrollees> loadByToken(
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

  public Optional<ParticipantUser> findByUsername(
      String username, EnvironmentName environmentName) {
    return participantUserDao.findOne(username, environmentName);
  }
}
