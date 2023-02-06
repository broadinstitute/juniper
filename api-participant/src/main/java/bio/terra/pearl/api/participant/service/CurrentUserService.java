package bio.terra.pearl.api.participant.service;

import bio.terra.pearl.core.dao.participant.ParticipantUserDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.ParticipantTaskService;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
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
      String username, EnvironmentName environmentName) {
    Optional<ParticipantUser> userOpt = participantUserDao.findOne(username, environmentName);
    if (userOpt.isPresent()) {
      ParticipantUser user = userOpt.get();
      user = updateUserToken(user);
      return Optional.of(loadFromUser(user));
    }
    return Optional.empty();
  }

  @Transactional
  public Optional<UserWithEnrollees> refresh(String token) {
    Optional<ParticipantUser> userOpt = participantUserDao.findByToken(token);
    if (userOpt.isPresent()) {
      ParticipantUser user = userOpt.get();
      user = updateUserToken(user);
      return Optional.of(loadFromUser(user));
    }
    return Optional.empty();
  }

  protected ParticipantUser updateUserToken(ParticipantUser user) {
    UUID newToken = UUID.randomUUID();
    user.setToken(newToken.toString());
    user.setLastLogin(Instant.now());
    return participantUserDao.update(user);
  }

  public UserWithEnrollees loadFromUser(ParticipantUser user) {
    user.getPortalParticipantUsers()
        .addAll(portalParticipantUserService.findByParticipantUserId(user.getId()));
    List<Enrollee> enrollees = enrolleeService.findByParticipantUserId(user.getId());
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
}
