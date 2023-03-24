package bio.terra.pearl.api.admin.service;

import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.service.portal.PortalService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PortalExtService {
  private PortalService portalService;
  private AuthUtilService authUtilService;

  public PortalExtService(PortalService portalService, AuthUtilService authUtilService) {
    this.portalService = portalService;
    this.authUtilService = authUtilService;
  }

  public Portal fullLoad(AdminUser user, String portalShortcode) {
    Portal portal = authUtilService.authUserToPortal(user, portalShortcode);
    return portalService.fullLoad(portal, "en");
  }

  public List<Portal> getAll(AdminUser user) {
    // no additional auth checks needed -- the underlying service filters out portals the user does
    // not have access to
    return portalService.findByAdminUserId(user.getId());
  }
}
