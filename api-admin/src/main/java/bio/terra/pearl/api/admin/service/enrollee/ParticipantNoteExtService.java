package bio.terra.pearl.api.admin.service.enrollee;

import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantNote;
import bio.terra.pearl.core.model.workflow.AdminTask;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import bio.terra.pearl.core.service.kit.KitRequestService;
import bio.terra.pearl.core.service.participant.ParticipantNoteService;
import bio.terra.pearl.core.service.workflow.AdminTaskService;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ParticipantNoteExtService {
  private AuthUtilService authUtilService;
  private KitRequestService kitRequestService;
  private ParticipantNoteService participantNoteService;
  private AdminTaskService adminTaskService;

  public ParticipantNoteExtService(
      AuthUtilService authUtilService,
      KitRequestService kitRequestService,
      ParticipantNoteService participantNoteService,
      AdminTaskService adminTaskService) {
    this.authUtilService = authUtilService;
    this.kitRequestService = kitRequestService;
    this.participantNoteService = participantNoteService;
    this.adminTaskService = adminTaskService;
  }

  @Transactional
  public ParticipantNote create(
      AdminUser user,
      String enrolleeShortcode,
      ParticipantNote participantNote,
      UUID assignedAdminUserId) {
    Enrollee enrollee = authUtilService.authAdminUserToEnrollee(user, enrolleeShortcode);
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
      AdminTask task =
          AdminTask.builder()
              .studyEnvironmentId(enrollee.getStudyEnvironmentId())
              .participantNoteId(savedNote.getId())
              .enrolleeId(enrollee.getId())
              .creatingAdminUserId(user.getId())
              .assignedAdminUserId(assignedAdminUserId)
              .status(TaskStatus.NEW)
              .build();
      adminTaskService.create(
          task, DataAuditInfo.builder().responsibleAdminUserId(user.getId()).build());
    }
    return savedNote;
  }
}
