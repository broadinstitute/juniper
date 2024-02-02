package bio.terra.pearl.api.participant.service;

import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import bio.terra.pearl.core.service.participant.EnrolleeRelationService;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.core.service.portal.PortalWithPortalUser;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AuthUtilService {
  private EnrolleeService enrolleeService;
  private EnrolleeRelationService enrolleeRelationService;
  private PortalService portalService;
  private PortalParticipantUserService portalParticipantUserService;

  public AuthUtilService(
      EnrolleeService enrolleeService,
      PortalService portalService,
      PortalParticipantUserService portalParticipantUserService,
      EnrolleeRelationService enrolleeRelationService) {
    this.enrolleeService = enrolleeService;
    this.portalService = portalService;
    this.portalParticipantUserService = portalParticipantUserService;
    this.enrolleeRelationService = enrolleeRelationService;
  }

  /**
   * returns the enrollee if the user is authorized to access/modify it, throws an error otherwise
   */
  public Enrollee authParticipantUserToEnrollee(UUID participantUserId, String enrolleeShortcode) {
    Optional<Enrollee> enrolleeOpt =
        enrolleeService.findByEnrolleeId(participantUserId, enrolleeShortcode);
    if (enrolleeOpt.isEmpty()) {
      return authParticipantUserToGovernedEnrollees(participantUserId, enrolleeShortcode);
    }
    return enrolleeOpt.get();
  }

  private Enrollee authParticipantUserToGovernedEnrollees(
      UUID participantUserId, String enrolleeShortcode) {
    return enrolleeRelationService.findGovernedEnrollees(participantUserId, null).stream()
        .filter(enrollee -> enrollee.getShortcode().equals(enrolleeShortcode))
        .findFirst()
        .orElseThrow(
            () ->
                new PermissionDeniedException("Access denied for %s".formatted(enrolleeShortcode)));
  }

  /** confirms the participant can access resources from the given portal */
  public PortalWithPortalUser authParticipantToPortal(
      UUID participantUserId, String portalShortcode, EnvironmentName envName) {
    Optional<Portal> portalOpt = portalService.findOneByShortcode(portalShortcode);
    if (portalOpt.isPresent()) {
      Portal portal = portalOpt.get();
      Optional<PortalParticipantUser> ppUser =
          portalParticipantUserService.findOne(participantUserId, portalShortcode, envName);
      if (ppUser.isPresent()) {
        return new PortalWithPortalUser(portal, ppUser.get());
      }
    }
    throw new PermissionDeniedException(
        "User %s does not have permissions on portal %s, env %s"
            .formatted(participantUserId, portalShortcode, envName));
  }
}
