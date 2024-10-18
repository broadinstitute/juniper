package bio.terra.pearl.api.admin.service.participant;

import bio.terra.pearl.api.admin.service.auth.EnforcePortalPermission;
import bio.terra.pearl.api.admin.service.auth.context.PortalEnvAuthContext;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.service.participant.ParticipantUserService;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import bio.terra.pearl.core.service.participant.merge.ParticipantMergePlanService;
import bio.terra.pearl.core.service.participant.merge.ParticipantMergeService;
import bio.terra.pearl.core.service.participant.merge.ParticipantUserMerge;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ParticipantMergeExtService {
  private final ParticipantMergePlanService participantMergePlanService;
  private final ParticipantMergeService participantMergeService;
  private final PortalParticipantUserService portalParticipantUserService;
  private final ParticipantUserService participantUserService;

  public ParticipantMergeExtService(
      ParticipantMergePlanService participantMergePlanService,
      ParticipantMergeService participantMergeService,
      PortalParticipantUserService portalParticipantUserService,
      ParticipantUserService participantUserService) {
    this.participantMergePlanService = participantMergePlanService;
    this.participantMergeService = participantMergeService;
    this.portalParticipantUserService = portalParticipantUserService;
    this.participantUserService = participantUserService;
  }

  @EnforcePortalPermission(permission = "participant_data_view")
  public ParticipantUserMerge plan(
      PortalEnvAuthContext authContext, ParticipantMergePlanRequest request) {
    ParticipantUser sourcePpUser = validateUserToPortal(authContext, request.sourceEmail);
    ParticipantUser targetPpUser = validateUserToPortal(authContext, request.targetEmail);
    return participantMergePlanService.planMerge(
        sourcePpUser, targetPpUser, authContext.getPortal());
  }

  @EnforcePortalPermission(permission = "participant_data_edit")
  public ParticipantUserMerge execute(PortalEnvAuthContext authContext, ParticipantUserMerge plan) {
    // confirm users, ppUsers, and tasks are in this portal and associated with each other
    validateUserToPortal(authContext, plan.getUsers().getSource().getId());
    validateUserToPortal(authContext, plan.getUsers().getTarget().getId());
    validatePPUser(authContext, plan.getPpUsers().getSource(), plan.getUsers().getSource());
    validatePPUser(authContext, plan.getPpUsers().getTarget(), plan.getUsers().getTarget());
    // the merge service handles confirming other elements of the plan are valid and consistent
    DataAuditInfo auditInfo =
        DataAuditInfo.builder()
            .responsibleAdminUserId(authContext.getOperator().getId())
            .portalParticipantUserId(plan.getPpUsers().getTarget().getId())
            .build();
    return participantMergeService.applyMerge(plan, auditInfo);
  }

  private void validatePPUser(
      PortalEnvAuthContext authContext, PortalParticipantUser ppUser, ParticipantUser user) {
    PortalParticipantUser fetchedPPUser =
        portalParticipantUserService
            .findOne(
                user.getId(),
                authContext.getPortal().getShortcode(),
                authContext.getEnvironmentName())
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "portal participant %s not found".formatted(ppUser.getId())));
    if (!fetchedPPUser.getId().equals(ppUser.getId())) {
      throw new IllegalArgumentException(
          "portal participant %s not matched".formatted(ppUser.getId(), user.getId()));
    }
  }

  public ParticipantUser validateUserToPortal(PortalEnvAuthContext authContext, String email) {
    PortalParticipantUser ppUser =
        portalParticipantUserService
            .findOne(
                email, authContext.getPortal().getShortcode(), authContext.getEnvironmentName())
            .orElseThrow(
                () -> new IllegalArgumentException("participant %s not found".formatted(email)));
    return participantUserService.find(ppUser.getParticipantUserId()).orElseThrow();
  }

  public ParticipantUser validateUserToPortal(
      PortalEnvAuthContext authContext, UUID participantUserId) {
    PortalParticipantUser ppUser =
        portalParticipantUserService
            .findOne(
                participantUserId,
                authContext.getPortal().getShortcode(),
                authContext.getEnvironmentName())
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "participant %s not found".formatted(participantUserId)));
    return participantUserService.find(ppUser.getParticipantUserId()).orElseThrow();
  }

  public record ParticipantMergePlanRequest(String sourceEmail, String targetEmail) {}
}
