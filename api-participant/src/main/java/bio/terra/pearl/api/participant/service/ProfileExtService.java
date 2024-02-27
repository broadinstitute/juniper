package bio.terra.pearl.api.participant.service;

import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import bio.terra.pearl.core.service.participant.ProfileService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import jakarta.ws.rs.NotFoundException;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ProfileExtService {
  private final AuthUtilService authUtilService;
  private final ProfileService profileService;
  private final PortalEnvironmentService portalEnvironmentService;

  public ProfileExtService(
      AuthUtilService authUtilService,
      ProfileService profileService,
      PortalEnvironmentService portalEnvironmentService) {
    this.authUtilService = authUtilService;
    this.profileService = profileService;
    this.portalEnvironmentService = portalEnvironmentService;
  }

  public Profile updateWithMailingAddress(
      String portalShortcode,
      EnvironmentName envName,
      ParticipantUser participantUser,
      UUID ppUserId,
      Profile profile) {

    PortalParticipantUser ppUser =
        authUtilService.authParticipantUserToPortalParticipantUser(
            participantUser.getId(), ppUserId);

    PortalEnvironment portalEnvironment =
        portalEnvironmentService
            .findOne(portalShortcode, envName)
            .orElseThrow(NotFoundException::new);

    if (!ppUser.getPortalEnvironmentId().equals(portalEnvironment.getId())) {
      throw new PermissionDeniedException("User not in portal environment");
    }

    profile.setId(ppUser.getProfileId());

    return profileService.updateWithMailingAddress(
        profile,
        DataAuditInfo.builder()
            .responsibleUserId(participantUser.getId())
            .portalParticipantUserId(ppUser.getId())
            .build());
  }

  public Profile findProfile(
      String portalShortcode,
      EnvironmentName envName,
      ParticipantUser participantUser,
      UUID ppUserId) {
    PortalParticipantUser ppUser =
        authUtilService.authParticipantUserToPortalParticipantUser(
            participantUser.getId(), ppUserId);

    PortalEnvironment portalEnvironment =
        portalEnvironmentService
            .findOne(portalShortcode, envName)
            .orElseThrow(NotFoundException::new);

    if (!ppUser.getPortalEnvironmentId().equals(portalEnvironment.getId())) {
      throw new PermissionDeniedException("User not in portal environment");
    }

    return profileService
        .loadWithMailingAddress(ppUser.getProfileId())
        .orElseThrow(NotFoundException::new);
  }
}
