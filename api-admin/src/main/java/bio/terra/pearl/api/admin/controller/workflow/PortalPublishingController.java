package bio.terra.pearl.api.admin.controller.workflow;

import bio.terra.pearl.api.admin.api.PortalPublishingApi;
import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.auth.context.PortalAuthContext;
import bio.terra.pearl.api.admin.service.auth.context.PortalEnvAuthContext;
import bio.terra.pearl.api.admin.service.portal.PortalExtService;
import bio.terra.pearl.api.admin.service.portal.PortalPublishingExtService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.publishing.PortalEnvironmentChange;
import bio.terra.pearl.core.model.publishing.PortalEnvironmentChangeRecord;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class PortalPublishingController implements PortalPublishingApi {
  private final HttpServletRequest request;
  private final AuthUtilService authUtilService;
  private final PortalPublishingExtService portalPublishingExtService;
  private final PortalExtService portalExtService;
  private final ObjectMapper objectMapper;

  public PortalPublishingController(
      HttpServletRequest request,
      AuthUtilService authUtilService,
      PortalPublishingExtService portalPublishingExtService,
      PortalExtService portalExtService,
      ObjectMapper objectMapper) {
    this.request = request;
    this.authUtilService = authUtilService;
    this.portalPublishingExtService = portalPublishingExtService;
    this.portalExtService = portalExtService;
    this.objectMapper = objectMapper;
  }

  @Override
  public ResponseEntity<Object> diff(String portalShortcode, String sourceEnv, String destEnv) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    return ResponseEntity.ok(
        portalPublishingExtService.diff(
            PortalAuthContext.of(operator, portalShortcode),
            EnvironmentName.valueOfCaseInsensitive(sourceEnv),
            EnvironmentName.valueOfCaseInsensitive(destEnv)));
  }

  @Override
  public ResponseEntity<Object> publish(String portalShortcode, String destEnv, Object body) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    PortalEnvironmentChange change = objectMapper.convertValue(body, PortalEnvironmentChange.class);
    return ResponseEntity.ok(
        portalPublishingExtService.publish(
            PortalEnvAuthContext.of(
                operator, portalShortcode, EnvironmentName.valueOfCaseInsensitive(destEnv)),
            change));
  }

  @Override
  public ResponseEntity<Object> getChangeRecords(String portalShortcode) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    List<PortalEnvironmentChangeRecord> records =
        portalPublishingExtService.getChangeRecords(
            PortalAuthContext.of(operator, portalShortcode));
    return ResponseEntity.ok(records);
  }
}
