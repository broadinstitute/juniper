package bio.terra.pearl.api.admin.service.enrollee;

import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantNote;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import bio.terra.pearl.core.model.workflow.TaskType;
import bio.terra.pearl.core.service.kit.KitRequestService;
import bio.terra.pearl.core.service.participant.ParticipantNoteService;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import bio.terra.pearl.core.service.workflow.ParticipantTaskService;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ParticipantNoteExtService {
  private final ParticipantTaskService participantTaskService;
  private final AuthUtilService authUtilService;
  private final KitRequestService kitRequestService;
  private final ParticipantNoteService participantNoteService;
  private final PortalParticipantUserService portalParticipantUserService;

  public ParticipantNoteExtService(
      AuthUtilService authUtilService,
      KitRequestService kitRequestService,
      ParticipantNoteService participantNoteService,
      ParticipantTaskService participantTaskService,
      PortalParticipantUserService portalParticipantUserService) {
    this.authUtilService = authUtilService;
    this.kitRequestService = kitRequestService;
    this.participantNoteService = participantNoteService;
    this.participantTaskService = participantTaskService;
    this.portalParticipantUserService = portalParticipantUserService;
  }

  @Transactional
  public ParticipantNote create(
      AdminUser user,
      String enrolleeShortcode,
      ParticipantNote participantNote,
      UUID assignedAdminUserId) {
    Enrollee enrollee = authUtilService.authAdminUserToEnrollee(user, enrolleeShortcode);
    PortalParticipantUser portalParticipantUser =
        portalParticipantUserService.findForEnrollee(enrollee);
    if (participantNote.getKitRequestId() != null) {
      KitRequest kitRequest = kitRequestService.find(participantNote.getKitRequestId()).get();
      if (kitRequest.getEnrolleeId().equals(enrollee.getId())) {
        throw new IllegalArgumentException("kit request does not match enrollee");
      }
    }
    participantNote.setCreatingAdminUserId(user.getId());
    participantNote.setEnrolleeId(enrollee.getId());
    ParticipantNote savedNote = participantNoteService.create(participantNote);
    if (assignedAdminUserId != null) {
      ParticipantTask task =
          ParticipantTask.builder()
              .studyEnvironmentId(enrollee.getStudyEnvironmentId())
              .participantNoteId(savedNote.getId())
              .portalParticipantUserId(portalParticipantUser.getId())
              .enrolleeId(enrollee.getId())
              .assignedAdminUserId(assignedAdminUserId)
              .taskType(TaskType.ADMIN_NOTE)
              .status(TaskStatus.NEW)
              .build();
      participantTaskService.create(
          task, DataAuditInfo.builder().responsibleAdminUserId(user.getId()).build());
    }
    return savedNote;
  }
}
