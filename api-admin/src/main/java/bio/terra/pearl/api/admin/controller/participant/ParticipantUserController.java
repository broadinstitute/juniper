package bio.terra.pearl.api.admin.controller.participant;

import bio.terra.pearl.api.admin.api.ParticipantUserApi;
import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.auth.context.PortalEnvAuthContext;
import bio.terra.pearl.api.admin.service.participant.ParticipantUserExtService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class ParticipantUserController implements ParticipantUserApi {
  private final ObjectMapper objectMapper;
  private AuthUtilService authUtilService;
  private ParticipantUserExtService participantUserExtService;
  private HttpServletRequest request;

  public ParticipantUserController(
      AuthUtilService authUtilService,
      ParticipantUserExtService participantUserExtService,
      HttpServletRequest request,
      ObjectMapper objectMapper) {
    this.authUtilService = authUtilService;
    this.participantUserExtService = participantUserExtService;
    this.request = request;
    this.objectMapper = objectMapper;
  }

  @Override
  public ResponseEntity<Object> list(String portalShortcode, String envName) {
    AdminUser user = authUtilService.requireAdminUser(request);
    return ResponseEntity.ok(
        this.participantUserExtService.list(
            PortalEnvAuthContext.of(
                user, portalShortcode, EnvironmentName.valueOfCaseInsensitive(envName))));
  }

  @Override
  public ResponseEntity<Object> findWithPortalUser(
      String portalShortcode, String envName, UUID participantUserId) {
    AdminUser user = authUtilService.requireAdminUser(request);
    return ResponseEntity.ok(
        this.participantUserExtService.findWithPortalUser(
            PortalEnvAuthContext.of(
                user, portalShortcode, EnvironmentName.valueOfCaseInsensitive(envName)),
            participantUserId));
  }

  @Override
  public ResponseEntity<Object> update(
      String portalShortcode, String envName, UUID participantUserId, Object body) {

    ParticipantUser participantUser = objectMapper.convertValue(body, ParticipantUser.class);
    AdminUser user = authUtilService.requireAdminUser(request);
    return ResponseEntity.ok(
        this.participantUserExtService.update(
            PortalEnvAuthContext.of(
                user, portalShortcode, EnvironmentName.valueOfCaseInsensitive(envName)),
            participantUserId,
            participantUser));
  }
}
