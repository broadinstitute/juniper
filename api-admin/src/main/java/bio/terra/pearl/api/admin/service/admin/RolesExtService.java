package bio.terra.pearl.api.admin.service.admin;

import bio.terra.pearl.api.admin.service.auth.AnyAdminUser;
import bio.terra.pearl.api.admin.service.auth.context.OperatorAuthContext;
import bio.terra.pearl.core.model.admin.Role;
import bio.terra.pearl.core.service.admin.RoleService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class RolesExtService {
  private final RoleService roleService;

  public RolesExtService(RoleService roleService) {
    this.roleService = roleService;
  }

  @AnyAdminUser
  public List<Role> list(OperatorAuthContext authContext) {
    List<Role> roles = roleService.findAll();
    roleService.attachPermissions(roles);
    return roles;
  }
}
