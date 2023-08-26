package bio.terra.pearl.api.participant.service;

import bio.terra.pearl.core.dao.participant.ParticipantUserDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.workflow.ParticipantTaskService;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import bio.terra.pearl.core.service.participant.ProfileService;
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
  private ProfileService profileService;

  public CurrentUserService(
      ParticipantUserDao participantUserDao,
      PortalParticipantUserService portalParticipantUserService,
      EnrolleeService enrolleeService,
      ParticipantTaskService participantTaskService,
      ProfileService profileService) {
    this.participantUserDao = participantUserDao;
    this.portalParticipantUserService = portalParticipantUserService;
    this.enrolleeService = enrolleeService;
    this.participantTaskService = participantTaskService;
    this.profileService = profileService;
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
      enrollee.setProfile(
          profileService.loadWithMailingAddress(enrollee.getProfileId()).orElse(null));
    }
    return new UserWithEnrollees(user, enrollees);
  }

  public record UserWithEnrollees(ParticipantUser user, List<Enrollee> enrollees) {}

  @Transactional
  public void logout(ParticipantUser user) {
    // we don't store token information locally yet (we might have to later if we take async
    // actions on their behalf). So for now, this is a no-op, B2C handles the logout mechanics
  }

  public Optional<ParticipantUser> findByUsername(
      String username, EnvironmentName environmentName) {
    return participantUserDao.findOne(username, environmentName);
  }
}
