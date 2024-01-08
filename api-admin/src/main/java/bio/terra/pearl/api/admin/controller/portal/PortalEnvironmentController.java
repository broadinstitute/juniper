package bio.terra.pearl.api.admin.controller.portal;

import bio.terra.pearl.api.admin.api.PortalEnvironmentApi;
import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.api.admin.service.portal.PortalExtService;
import bio.terra.pearl.api.admin.service.portal.PortalPublishingExtService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.publishing.PortalEnvironmentChange;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class PortalEnvironmentController implements PortalEnvironmentApi {
  private HttpServletRequest request;
  private AuthUtilService authUtilService;
  private PortalPublishingExtService portalPublishingExtService;
  private PortalExtService portalExtService;
  private ObjectMapper objectMapper;

  public PortalEnvironmentController(
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
  public ResponseEntity<Object> update(String portalShortcode, String envName, Object body) {
    AdminUser user = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    PortalEnvironment portalEnv = objectMapper.convertValue(body, PortalEnvironment.class);
    PortalEnvironment updatedEnv =
        portalExtService.updateEnvironment(portalShortcode, environmentName, portalEnv, user);
    return ResponseEntity.ok(updatedEnv);
  }

  @Override
  public ResponseEntity<Object> diff(String portalShortcode, String destEnv, String sourceEnv) {
    AdminUser user = authUtilService.requireAdminUser(request);
    return ResponseEntity.ok(
        portalPublishingExtService.diff(
            portalShortcode,
            EnvironmentName.valueOfCaseInsensitive(sourceEnv),
            EnvironmentName.valueOfCaseInsensitive(destEnv),
            user));
  }

  @Override
  public ResponseEntity<Object> apply(
      String portalShortcode, String destEnv, String sourceEnv, Object body) {
    AdminUser user = authUtilService.requireAdminUser(request);
    PortalEnvironmentChange change = objectMapper.convertValue(body, PortalEnvironmentChange.class);
    return ResponseEntity.ok(
        portalPublishingExtService.update(
            portalShortcode, EnvironmentName.valueOfCaseInsensitive(destEnv), change, user));
  }
}
