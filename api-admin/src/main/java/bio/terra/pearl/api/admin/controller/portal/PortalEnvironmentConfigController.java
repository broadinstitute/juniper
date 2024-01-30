package bio.terra.pearl.api.admin.controller.portal;

import bio.terra.pearl.api.admin.api.PortalEnvironmentConfigApi;
import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.api.admin.service.portal.PortalExtService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.portal.PortalEnvironmentConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class PortalEnvironmentConfigController implements PortalEnvironmentConfigApi {
  private HttpServletRequest request;
  private AuthUtilService authUtilService;
  private PortalExtService portalExtService;
  private ObjectMapper objectMapper;

  public PortalEnvironmentConfigController(
      HttpServletRequest request,
      AuthUtilService authUtilService,
      PortalExtService portalExtService,
      ObjectMapper objectMapper) {
    this.request = request;
    this.authUtilService = authUtilService;
    this.portalExtService = portalExtService;
    this.objectMapper = objectMapper;
  }

  @Override
  public ResponseEntity<Object> update(String portalShortcode, String envName, Object configObj) {
    AdminUser user = authUtilService.requireAdminUser(request);
    PortalEnvironmentConfig config =
        objectMapper.convertValue(configObj, PortalEnvironmentConfig.class);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
      PortalEnvironmentConfig updatedConfig =
        portalExtService.updateConfig(portalShortcode, environmentName, config, user);
    return ResponseEntity.ok(updatedConfig);
  }
}
