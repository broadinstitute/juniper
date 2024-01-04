package bio.terra.pearl.api.admin.controller.internal;

import bio.terra.pearl.api.admin.api.ConfigApi;
import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.api.admin.service.ConfigExtService;
import bio.terra.pearl.core.model.admin.AdminUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

/** for returning config data to the UI for debugging */
@Controller
public class ConfigController implements ConfigApi {
  private HttpServletRequest request;
  private AuthUtilService authUtilService;
  private ConfigExtService configExtService;

  public ConfigController(
      HttpServletRequest request,
      AuthUtilService authUtilService,
      ConfigExtService configExtService) {
    this.request = request;
    this.authUtilService = authUtilService;
    this.configExtService = configExtService;
  }

  @Override
  public ResponseEntity<Object> get() {
    AdminUser user = authUtilService.requireAdminUser(request);
    return ResponseEntity.ok(configExtService.getInternalConfigMap(user));
  }
}
