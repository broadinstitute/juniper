package bio.terra.pearl.api.admin.service;

import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.publishing.PortalEnvironmentChange;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import bio.terra.pearl.core.service.publishing.PortalDiffService;
import bio.terra.pearl.core.service.publishing.PortalUpdateService;
import org.springframework.stereotype.Service;

@Service
public class PortalPublishingExtService {
  private AuthUtilService authUtilService;
  private PortalDiffService portalDiffService;
  private PortalUpdateService portalUpdateService;

  public PortalPublishingExtService(
      AuthUtilService authUtilService,
      PortalDiffService portalDiffService,
      PortalUpdateService portalUpdateService) {
    this.authUtilService = authUtilService;
    this.portalDiffService = portalDiffService;
    this.portalUpdateService = portalUpdateService;
  }

  public PortalEnvironmentChange diff(
      String portalShortcode, EnvironmentName destEnv, EnvironmentName sourceEnv, AdminUser user) {
    authUtilService.authUserToPortal(user, portalShortcode);
    try {
      return portalDiffService.diffPortalEnvs(portalShortcode, destEnv, sourceEnv);
    } catch (Exception e) {
      throw new IllegalArgumentException(e);
    }
  }

  public PortalEnvironment update(
      String portalShortcode,
      EnvironmentName destEnv,
      PortalEnvironmentChange change,
      AdminUser user) {
    authUtilService.authUserToPortal(user, portalShortcode);
    if (!user.isSuperuser()) {
      throw new PermissionDeniedException("You do not have permission to update environments");
    }
    try {
      return portalUpdateService.applyChanges(portalShortcode, destEnv, change, user);
    } catch (Exception e) {
      throw new IllegalArgumentException(e);
    }
  }
}
