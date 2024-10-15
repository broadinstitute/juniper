package bio.terra.pearl.api.admin.controller.participant;

import bio.terra.pearl.api.admin.api.ParticipantUserApi;
import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.auth.context.PortalEnvAuthContext;
import bio.terra.pearl.api.admin.service.participant.ParticipantUserExtService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class ParticipantUserController implements ParticipantUserApi {
  private AuthUtilService authUtilService;
  private ParticipantUserExtService participantUserExtService;
  private HttpServletRequest request;

  public ParticipantUserController(
      AuthUtilService authUtilService,
      ParticipantUserExtService participantUserExtService,
      HttpServletRequest request) {
    this.authUtilService = authUtilService;
    this.participantUserExtService = participantUserExtService;
    this.request = request;
  }

  @Override
  public ResponseEntity<Object> list(String portalShortcode, String envName) {
    AdminUser user = authUtilService.requireAdminUser(request);
    return ResponseEntity.ok(
        this.participantUserExtService.list(
            PortalEnvAuthContext.of(
                user, portalShortcode, EnvironmentName.valueOfCaseInsensitive(envName))));
  }
}
