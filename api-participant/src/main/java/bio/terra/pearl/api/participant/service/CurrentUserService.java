package bio.terra.pearl.api.participant.service;

import bio.terra.common.exception.UnauthorizedException;
import bio.terra.pearl.core.dao.participant.ParticipantUserDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.*;
import bio.terra.pearl.core.service.participant.EnrolleeRelationService;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import bio.terra.pearl.core.service.participant.ProfileService;
import bio.terra.pearl.core.service.workflow.ParticipantTaskService;
import bio.terra.pearl.core.service.workflow.RegistrationService;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class CurrentUserService {
  private final ParticipantUserDao participantUserDao;
  private final PortalParticipantUserService portalParticipantUserService;
  private final EnrolleeService enrolleeService;
  private final EnrolleeRelationService enrolleeRelationService;
  private final ParticipantTaskService participantTaskService;
  private final ProfileService profileService;
  private final RegistrationService registrationService;

  public CurrentUserService(
      ParticipantUserDao participantUserDao,
      PortalParticipantUserService portalParticipantUserService,
      EnrolleeService enrolleeService,
      EnrolleeRelationService enrolleeRelationService,
      ParticipantTaskService participantTaskService,
      ProfileService profileService,
      RegistrationService registrationService) {
    this.participantUserDao = participantUserDao;
    this.portalParticipantUserService = portalParticipantUserService;
    this.enrolleeService = enrolleeService;
    this.enrolleeRelationService = enrolleeRelationService;
    this.participantTaskService = participantTaskService;
    this.profileService = profileService;
    this.registrationService = registrationService;
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
    user.ppUsers.forEach(
        ppUser -> {
          ppUser.setLastLogin(Instant.now());
          portalParticipantUserService.update(ppUser);
        });
    return user;
  }

  public String getUsernameFromToken(String token) {
    DecodedJWT decodedJWT = JWT.decode(token);
    return decodedJWT.getClaim("email").asString();
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
    user.getPortalParticipantUsers().add(ppUser);

    List<Enrollee> enrollees = loadEnrollees(ppUser);

    List<EnrolleeRelation> proxyRelations = loadProxyRelations(enrollees);
    enrollees.addAll(proxyRelations.stream().map(EnrolleeRelation::getTargetEnrollee).toList());

    List<PortalParticipantUser> ppUsers = new ArrayList<>();
    ppUsers.add(ppUser);

    // Load the main user's proxiable ppUsers
    enrollees.forEach(
        enrollee -> {
          if (ppUsers.stream()
              .anyMatch(ppu -> ppu.getProfileId().equals(enrollee.getProfileId()))) {
            return;
          }

          Optional<PortalParticipantUser> proxyUser =
              portalParticipantUserService.findByProfileId(enrollee.getProfileId());
          proxyUser.ifPresent(ppUsers::add);
        });

    return new UserLoginDto(
        user, profileService.loadProfile(ppUser), ppUsers, enrollees, proxyRelations);
  }

  private List<Enrollee> loadEnrollees(PortalParticipantUser ppUser) {
    List<Enrollee> enrollees = enrolleeService.findByPortalParticipantUser(ppUser);
    for (Enrollee enrollee : enrollees) {
      enrolleeService.loadForParticipantDashboard(enrollee);
    }
    return enrollees;
  }

  private List<EnrolleeRelation> loadProxyRelations(List<Enrollee> enrollees) {
    List<EnrolleeRelation> relations = new ArrayList<>();
    if (!enrollees.isEmpty()) {
      relations =
          enrolleeRelationService.findByEnrolleeIdsAndRelationType(
              enrollees.stream().map(Enrollee::getId).toList(), RelationshipType.PROXY);
      enrolleeRelationService.attachTargetEnrollees(relations);
      for (EnrolleeRelation relation : relations) {
        enrolleeService.loadForParticipantDashboard(relation.getTargetEnrollee());
      }
    }

    return relations;
  }

  @Transactional
  public CurrentUserService.UserLoginDto registerOrLogin(
      String token,
      String portalShortcode,
      EnvironmentName environmentName,
      UUID preRegResponseId,
      String preferredLanguage) {

    String email = getUsernameFromToken(token);
    if (portalParticipantUserService.findOne(email, portalShortcode, environmentName).isEmpty()) {
      registrationService.register(
          portalShortcode, environmentName, email, preRegResponseId, preferredLanguage);
    }
    return tokenLogin(token, portalShortcode, environmentName);
  }

  @Transactional
  public void logout(ParticipantUser user) {
    // we don't store token information locally yet (we might have to later if we take async
    // actions on their behalf). So for now, this is a no-op, B2C handles the logout mechanics
  }

  public Optional<ParticipantUser> findByUsername(
      String username, EnvironmentName environmentName) {
    return participantUserDao.findOne(username, environmentName);
  }

  public record UserLoginDto(
      ParticipantUser user,
      Profile profile,
      List<PortalParticipantUser> ppUsers, // includes proxied ppusers
      List<Enrollee> enrollees,
      List<EnrolleeRelation> proxyRelations) {}
}
