package bio.terra.pearl.api.participant.service;

import bio.terra.common.exception.UnauthorizedException;
import bio.terra.pearl.core.dao.participant.ParticipantUserDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.EnrolleeRelation;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.participant.RelationshipType;
import bio.terra.pearl.core.service.participant.EnrolleeRelationService;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import bio.terra.pearl.core.service.participant.ProfileService;
import bio.terra.pearl.core.service.workflow.ParticipantTaskService;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class CurrentUserService {
  private ParticipantUserDao participantUserDao;
  private PortalParticipantUserService portalParticipantUserService;
  private EnrolleeService enrolleeService;
  private EnrolleeRelationService enrolleeRelationService;
  private ParticipantTaskService participantTaskService;
  private ProfileService profileService;

  public CurrentUserService(
      ParticipantUserDao participantUserDao,
      PortalParticipantUserService portalParticipantUserService,
      EnrolleeService enrolleeService,
      EnrolleeRelationService enrolleeRelationService,
      ParticipantTaskService participantTaskService,
      ProfileService profileService) {
    this.participantUserDao = participantUserDao;
    this.portalParticipantUserService = portalParticipantUserService;
    this.enrolleeService = enrolleeService;
    this.enrolleeRelationService = enrolleeRelationService;
    this.participantTaskService = participantTaskService;
    this.profileService = profileService;
  }

  /**
   * Validate token and verify user represented in token is authorized for portal. If so update user
   * last login time
   */
  @Transactional
  public UserLoginDto tokenLogin(
      String token, String portalShortcode, EnvironmentName environmentName) {
    UserLoginDto user = loadByToken(token, portalShortcode, environmentName);
    user.user.setLastLogin(Instant.now());
    participantUserDao.update(user.user);
    return user;
  }

  @Transactional
  public UserLoginDto refresh(
      String token, String portalShortcode, EnvironmentName environmentName) {
    return loadByToken(token, portalShortcode, environmentName);
  }

  protected UserLoginDto loadByToken(
      String token, String portalShortcode, EnvironmentName environmentName) {
    DecodedJWT decodedJWT = JWT.decode(token);
    String email = decodedJWT.getClaim("email").asString();
    ParticipantUser user =
        participantUserDao
            .findOne(email, environmentName)
            .orElseThrow(
                () ->
                    new UnauthorizedException("User not found for environment " + environmentName));
    if (!user.isLoginAllowed()) {
      throw new UnauthorizedException("Login not allowed for this account");
    }
    return loadFromUser(user, portalShortcode);
  }

  public UserLoginDto loadFromUser(ParticipantUser user, String portalShortcode) {
    Optional<PortalParticipantUser> portalParticipantUser =
        portalParticipantUserService.findOne(user.getId(), portalShortcode);
    if (portalParticipantUser.isEmpty()) {
      log.info("User {} not found for portal {}", user.getId(), portalShortcode);
      throw new UnauthorizedException("User not found for portal " + portalShortcode);
    }
    PortalParticipantUser ppUser = portalParticipantUser.get();

    Profile profile =
        profileService
            .loadWithMailingAddress(ppUser.getProfileId())
            .orElseThrow(IllegalStateException::new);
    user.getPortalParticipantUsers().add(ppUser);
    List<Enrollee> enrollees = enrolleeService.findByPortalParticipantUser(ppUser);
    for (Enrollee enrollee : enrollees) {
      enrolleeService.loadForParticipantDashboard(enrollee);
    }
    List<EnrolleeRelation> proxyRelations =
        enrolleeRelationService.findByEnrolleeIdsAndRelationType(
            enrollees.stream().map(Enrollee::getId).toList(), RelationshipType.PROXY);
    enrolleeRelationService.attachTargetEnrollees(proxyRelations);
    for (EnrolleeRelation relation : proxyRelations) {
      enrolleeService.loadForParticipantDashboard(relation.getTargetEnrollee());
    }
    return new UserLoginDto(user, ppUser, profile, enrollees, proxyRelations);
  }

  public record UserLoginDto(
      ParticipantUser user,
      PortalParticipantUser ppUser,
      Profile profile,
      List<Enrollee> enrollees,
      List<EnrolleeRelation> relations) {}

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
