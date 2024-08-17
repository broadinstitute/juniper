package bio.terra.pearl.api.admin.controller.admin;

import bio.terra.pearl.api.admin.api.RolesApi;
import bio.terra.pearl.api.admin.service.admin.RolesExtService;
import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.auth.context.OperatorAuthContext;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.admin.Role;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class RolesController implements RolesApi {
  private final RolesExtService rolesExtService;
  private final AuthUtilService authUtilService;
  private final HttpServletRequest request;

  public RolesController(
      RolesExtService rolesExtService,
      AuthUtilService authUtilService,
      HttpServletRequest request) {
    this.rolesExtService = rolesExtService;
    this.authUtilService = authUtilService;
    this.request = request;
  }

  @Override
  public ResponseEntity<Object> list() {
    AdminUser user = authUtilService.requireAdminUser(request);
    List<Role> roles = rolesExtService.list(OperatorAuthContext.of(user));
    return ResponseEntity.ok(roles);
  }
}
