package bio.terra.pearl.api.admin.service.portal;

import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.portal.PortalEnvironmentConfig;
import bio.terra.pearl.core.service.admin.PortalAdminUserService;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import bio.terra.pearl.core.service.portal.PortalEnvironmentConfigService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.core.service.portal.exception.PortalConfigMissing;
import bio.terra.pearl.core.service.portal.exception.PortalEnvironmentMissing;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class PortalExtService {
  private PortalService portalService;
  private PortalEnvironmentService portalEnvironmentService;
  private PortalEnvironmentConfigService portalEnvironmentConfigService;
  private PortalAdminUserService portalAdminUserService;
  private AuthUtilService authUtilService;

  public PortalExtService(
      PortalService portalService,
      PortalEnvironmentService portalEnvironmentService,
      PortalEnvironmentConfigService portalEnvironmentConfigService,
      PortalAdminUserService portalAdminUserService,
      AuthUtilService authUtilService) {
    this.portalService = portalService;
    this.portalEnvironmentService = portalEnvironmentService;
    this.portalEnvironmentConfigService = portalEnvironmentConfigService;
    this.portalAdminUserService = portalAdminUserService;
    this.authUtilService = authUtilService;
  }

  public Portal fullLoad(AdminUser user, String portalShortcode, String language) {
    Portal portal = authUtilService.authUserToPortal(user, portalShortcode);
    return portalService.fullLoad(portal, language);
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
    authUtilService.authUserToPortal(user, portalShortcode);
    PortalEnvironment portalEnv =
        portalEnvironmentService
            .findOne(portalShortcode, envName)
            .orElseThrow(PortalEnvironmentMissing::new);
    PortalEnvironmentConfig config =
        portalEnvironmentConfigService
            .find(portalEnv.getPortalEnvironmentConfigId())
            .orElseThrow(PortalConfigMissing::new);
    BeanUtils.copyProperties(newConfig, config, "id", "createdAt");
    config = portalEnvironmentConfigService.update(config);
    return config;
  }

  /**
   * updates a portal environment, currently only supports updating the siteContent and preReg
   * survey
   */
  public PortalEnvironment updateEnvironment(
      String portalShortcode,
      EnvironmentName envName,
      PortalEnvironment updatedEnv,
      AdminUser user) {

    Portal portal = authUtilService.authUserToPortal(user, portalShortcode);
    if (!EnvironmentName.sandbox.equals(envName)) {
      throw new IllegalArgumentException("You cannot directly update non-sandbox environments");
    }
    if (updatedEnv.getPreRegSurveyId() != null) {
      authUtilService.authSurveyToPortal(portal, updatedEnv.getPreRegSurveyId());
    }
    PortalEnvironment portalEnv =
        portalEnvironmentService
            .findOne(portalShortcode, envName)
            .orElseThrow(PortalEnvironmentMissing::new);
    portalEnv.setSiteContentId(updatedEnv.getSiteContentId());
    portalEnv.setPreRegSurveyId(updatedEnv.getPreRegSurveyId());
    return portalEnvironmentService.update(portalEnv);
  }

  public void removeUserFromPortal(UUID adminUserId, String portalShortcode, AdminUser operator) {
    Portal portal = authUtilService.authUserToPortal(operator, portalShortcode);
    portalAdminUserService.removeUserFromPortal(adminUserId, portal);
  }
}
