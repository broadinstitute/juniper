package bio.terra.pearl.api.admin.controller.enrollee;

import bio.terra.pearl.api.admin.api.ParticipantNoteApi;
import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.api.admin.service.enrollee.ParticipantNoteExtService;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.participant.ParticipantNote;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class ParticipantNoteController implements ParticipantNoteApi {
  private AuthUtilService authUtilService;
  private ParticipantNoteExtService participantNoteExtService;
  private HttpServletRequest request;
  private ObjectMapper objectMapper;

  public ParticipantNoteController(
      AuthUtilService authUtilService,
      ParticipantNoteExtService participantNoteExtService,
      HttpServletRequest request,
      ObjectMapper objectMapper) {
    this.authUtilService = authUtilService;
    this.participantNoteExtService = participantNoteExtService;
    this.request = request;
    this.objectMapper = objectMapper;
  }

  @Override
  public ResponseEntity<Object> create(
      String portalShortcode,
      String studyShortcode,
      String envName,
      String enrolleeShortcode,
      Object body) {
    AdminUser adminUser = authUtilService.requireAdminUser(request);
    ParticipantNoteDto note = objectMapper.convertValue(body, ParticipantNoteDto.class);
    ParticipantNote savedNote =
        participantNoteExtService.create(
            adminUser, enrolleeShortcode, note, note.assignedAdminUserId);
    return ResponseEntity.ok(savedNote);
  }

  public static class ParticipantNoteDto extends ParticipantNote {
    public UUID assignedAdminUserId;
  }
}
