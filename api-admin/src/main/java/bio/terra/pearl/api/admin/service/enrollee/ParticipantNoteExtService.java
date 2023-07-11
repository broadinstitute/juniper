package bio.terra.pearl.api.admin.service.enrollee;

import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantNote;
import bio.terra.pearl.core.service.kit.KitRequestService;
import bio.terra.pearl.core.service.participant.ParticipantNoteService;
import org.springframework.stereotype.Service;

@Service
public class ParticipantNoteExtService {
  private AuthUtilService authUtilService;
  private KitRequestService kitRequestService;
  private ParticipantNoteService participantNoteService;

  public ParticipantNoteExtService(
      AuthUtilService authUtilService,
      KitRequestService kitRequestService,
      ParticipantNoteService participantNoteService) {
    this.authUtilService = authUtilService;
    this.kitRequestService = kitRequestService;
    this.participantNoteService = participantNoteService;
  }

  public ParticipantNote create(
      AdminUser user, String enrolleeShortcode, ParticipantNote participantNote) {
    Enrollee enrollee = authUtilService.authAdminUserToEnrollee(user, enrolleeShortcode);
    if (participantNote.getKitRequestId() != null) {
      var kitRequest = kitRequestService.find(participantNote.getKitRequestId()).get();
      if (kitRequest.getEnrolleeId().equals(enrollee.getId())) {
        throw new IllegalArgumentException("kit request does not match enrollee");
      }
    }
    participantNote.setCreatingAdminUserId(user.getId());
    participantNote.setEnrolleeId(enrollee.getId());
    return participantNoteService.create(participantNote);
  }
}
