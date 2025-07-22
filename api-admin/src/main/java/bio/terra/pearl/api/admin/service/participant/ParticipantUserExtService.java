package bio.terra.pearl.api.admin.service.participant;

import bio.terra.pearl.api.admin.service.auth.EnforcePortalEnvPermission;
import bio.terra.pearl.api.admin.service.auth.SuperuserOnly;
import bio.terra.pearl.api.admin.service.auth.context.PortalEnvAuthContext;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.ParticipantUserService;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ParticipantUserExtService {
  private final ParticipantUserService participantUserService;
  private final EnrolleeService enrolleeService;
  private final PortalParticipantUserService portalParticipantUserService;

  public ParticipantUserExtService(
      ParticipantUserService participantUserService,
      EnrolleeService enrolleeService,
      PortalParticipantUserService portalParticipantUserService) {
    this.participantUserService = participantUserService;
    this.enrolleeService = enrolleeService;
    this.portalParticipantUserService = portalParticipantUserService;
  }

  @EnforcePortalEnvPermission(permission = "participant_data_view")
  public ParticipantUsersWithEnrollees list(PortalEnvAuthContext authContext) {
    List<ParticipantUser> participantUsers =
        participantUserService.findAllByPortalEnv(
            authContext.getPortal().getId(), authContext.getEnvironmentName());
    List<Enrollee> enrollees =
        enrolleeService.findAllByPortalEnv(
            authContext.getPortal().getId(), authContext.getEnvironmentName());
    enrolleeService.attachProfiles(enrollees);
    return new ParticipantUsersWithEnrollees(participantUsers, enrollees);
  }

  @EnforcePortalEnvPermission(permission = "participant_data_view")
  public ParticipantUser findWithPortalUser(
      PortalEnvAuthContext authContext, UUID participantUserId) {
    ParticipantUser participantUser =
        participantUserService
            .find(participantUserId)
            .orElseThrow(() -> new NotFoundException("Participant user not found"));
    // we throw the same exception if the PortalParticipantUser isn't found, to prevent
    // admins from seeing information from other portals
    PortalParticipantUser portalParticipantUser =
        portalParticipantUserService
            .findOne(participantUser.getId(), authContext.getPortalEnvironment().getId())
            .orElseThrow(() -> new NotFoundException("Participant user not found"));
    participantUser.setPortalParticipantUsers(List.of(portalParticipantUser));
    return participantUser;
  }

  /** updates the participantUser -- currently only supports editing the username */
  @EnforcePortalEnvPermission(permission = "participant_data_edit")
  @SuperuserOnly
  public ParticipantUser update(
      PortalEnvAuthContext authContext, UUID participantUserId, ParticipantUser updatedUser) {

    ParticipantUser participantUser =
        participantUserService
            .find(participantUserId)
            .orElseThrow(() -> new NotFoundException("Participant user not found"));

    // since this is superuser-only, mostly checking as a sanity check that we're in the right
    // environment
    portalParticipantUserService
        .findOne(participantUser.getId(), authContext.getPortalEnvironment().getId())
        .orElseThrow(() -> new NotFoundException("Participant user not found"));

    participantUser.setUsername(updatedUser.getUsername());
    return participantUserService.update(participantUser);
  }
}
