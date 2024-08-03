package bio.terra.pearl.api.admin.controller.portal;

import bio.terra.pearl.api.admin.api.PortalEnvironmentApi;
import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.portal.PortalExtService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.portal.PortalEnvironmentLanguage;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class PortalEnvironmentController implements PortalEnvironmentApi {
  private final HttpServletRequest request;
  private final AuthUtilService authUtilService;
  private final PortalExtService portalExtService;
  private final ObjectMapper objectMapper;

  public PortalEnvironmentController(
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
  public ResponseEntity<Object> update(String portalShortcode, String envName, Object body) {
    AdminUser user = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    PortalEnvironment portalEnv = objectMapper.convertValue(body, PortalEnvironment.class);
    PortalEnvironment updatedEnv =
        portalExtService.updateEnvironment(portalShortcode, environmentName, portalEnv, user);
    return ResponseEntity.ok(updatedEnv);
  }

  @Override
  public ResponseEntity<Object> setLanguages(String portalShortcode, String envName, Object body) {
    AdminUser user = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    List<PortalEnvironmentLanguage> updatedLanguages =
        objectMapper.convertValue(body, new TypeReference<List<PortalEnvironmentLanguage>>() {});
    updatedLanguages =
        portalExtService.setLanguages(portalShortcode, environmentName, updatedLanguages, user);
    return ResponseEntity.ok(updatedLanguages);
  }
}
