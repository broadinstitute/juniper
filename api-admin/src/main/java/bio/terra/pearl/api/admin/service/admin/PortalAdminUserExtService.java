package bio.terra.pearl.api.admin.service.admin;

import bio.terra.pearl.api.admin.service.auth.EnforcePortalPermission;
import bio.terra.pearl.api.admin.service.auth.context.PortalAuthContext;
import bio.terra.pearl.core.model.admin.PortalAdminUser;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.admin.PortalAdminUserService;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class PortalAdminUserExtService {
  private final PortalAdminUserService portalAdminUserService;

  public PortalAdminUserExtService(PortalAdminUserService portalAdminUserService) {
    this.portalAdminUserService = portalAdminUserService;
  }

  @EnforcePortalPermission(permission = "admin_user_edit")
  public void create(PortalAuthContext authContext, PortalAdminUser portalAdminUser) {
    portalAdminUserService.create(portalAdminUser, null);
  }

  @EnforcePortalPermission(permission = "admin_user_edit")
  public void delete(PortalAuthContext authContext, UUID id) {
    portalAdminUserService.delete(id, null, CascadeProperty.EMPTY_SET);
  }
}
