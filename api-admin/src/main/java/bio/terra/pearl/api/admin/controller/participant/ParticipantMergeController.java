package bio.terra.pearl.api.admin.controller.participant;

import bio.terra.pearl.api.admin.api.ParticipantMergeApi;
import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.auth.context.PortalEnvAuthContext;
import bio.terra.pearl.api.admin.service.participant.ParticipantMergeExtService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.service.participant.merge.ParticipantUserMerge;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class ParticipantMergeController implements ParticipantMergeApi {
  private AuthUtilService authUtilService;
  private ParticipantMergeExtService participantMergeExtService;
  private HttpServletRequest request;
  private final ObjectMapper objectMapper;

  public ParticipantMergeController(
      AuthUtilService authUtilService,
      ParticipantMergeExtService participantMergeExtService,
      HttpServletRequest request,
      ObjectMapper objectMapper) {
    this.authUtilService = authUtilService;
    this.participantMergeExtService = participantMergeExtService;
    this.request = request;
    this.objectMapper = objectMapper;
  }

  @Override
  public ResponseEntity<Object> plan(String portalShortcode, String envName, Object body) {
    AdminUser user = authUtilService.requireAdminUser(request);
    ParticipantMergeExtService.ParticipantMergePlanRequest planRequest =
        objectMapper.convertValue(
            body, ParticipantMergeExtService.ParticipantMergePlanRequest.class);
    return ResponseEntity.ok(
        this.participantMergeExtService.plan(
            PortalEnvAuthContext.of(
                user, portalShortcode, EnvironmentName.valueOfCaseInsensitive(envName)),
            planRequest));
  }

  @Override
  public ResponseEntity<Object> execute(String portalShortcode, String envName, Object body) {
    AdminUser user = authUtilService.requireAdminUser(request);
    ParticipantUserMerge merge = objectMapper.convertValue(body, ParticipantUserMerge.class);
    return ResponseEntity.ok(
        this.participantMergeExtService.execute(
            PortalEnvAuthContext.of(
                user, portalShortcode, EnvironmentName.valueOfCaseInsensitive(envName)),
            merge));
  }
}
