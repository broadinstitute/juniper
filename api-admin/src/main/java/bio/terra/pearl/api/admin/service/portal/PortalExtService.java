package bio.terra.pearl.api.admin.service.portal;

import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.portal.PortalEnvironmentConfig;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import bio.terra.pearl.core.service.portal.PortalEnvironmentConfigService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import bio.terra.pearl.core.service.portal.PortalService;
import java.util.List;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class PortalExtService {
  private PortalService portalService;
  private PortalEnvironmentService portalEnvironmentService;
  private PortalEnvironmentConfigService portalEnvironmentConfigService;
  private AuthUtilService authUtilService;

  public PortalExtService(
      PortalService portalService,
      PortalEnvironmentService portalEnvironmentService,
      PortalEnvironmentConfigService portalEnvironmentConfigService,
      AuthUtilService authUtilService) {
    this.portalService = portalService;
    this.portalEnvironmentService = portalEnvironmentService;
    this.portalEnvironmentConfigService = portalEnvironmentConfigService;
    this.authUtilService = authUtilService;
  }

  public Portal fullLoad(AdminUser user, String portalShortcode) {
    Portal portal = authUtilService.authUserToPortal(user, portalShortcode);
    return portalService.fullLoad(portal, "en");
  }

  /** gets all the portals the user has access to, and attaches the corresponding studies */
  public List<Portal> getAll(AdminUser user) {
    // no additional auth checks needed -- the underlying service filters out portals the user does
    // not have access to
    List<Portal> portals = portalService.findByAdminUser(user);
    portalService.attachStudies(portals);
    return portals;
  }

  public PortalEnvironmentConfig updateConfig(
      String portalShortcode,
      EnvironmentName envName,
      PortalEnvironmentConfig newConfig,
      AdminUser user) {
    if (!user.isSuperuser()) {
      throw new PermissionDeniedException(
          "You do not have permissions to update portal configurations");
    }
    Portal portal = authUtilService.authUserToPortal(user, portalShortcode);
    PortalEnvironment portalEnv = portalEnvironmentService.findOne(portalShortcode, envName).get();
    PortalEnvironmentConfig config =
        portalEnvironmentConfigService.find(portalEnv.getPortalEnvironmentConfigId()).get();
    BeanUtils.copyProperties(newConfig, config, "id", "createdAt");
    config = portalEnvironmentConfigService.update(config);
    return config;
  }

  /** updates a portal environment, currently only supports updating the siteContent */
  public PortalEnvironment updateEnvironment(
      String portalShortcode,
      EnvironmentName envName,
      PortalEnvironment updatedEnv,
      AdminUser user) {

    authUtilService.authUserToPortal(user, portalShortcode);
    PortalEnvironment portalEnv = portalEnvironmentService.findOne(portalShortcode, envName).get();
    portalEnv.setSiteContentId(updatedEnv.getSiteContentId());
    return portalEnvironmentService.update(portalEnv);
  }
}
