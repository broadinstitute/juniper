package bio.terra.pearl.api.admin.service.portal;

import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.publishing.PortalEnvironmentChange;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import bio.terra.pearl.core.service.publishing.PortalDiffService;
import bio.terra.pearl.core.service.publishing.PortalPublishingService;
import org.springframework.stereotype.Service;

@Service
public class PortalPublishingExtService {
  private AuthUtilService authUtilService;
  private PortalDiffService portalDiffService;
  private PortalPublishingService portalPublishingService;

  public PortalPublishingExtService(
      AuthUtilService authUtilService,
      PortalDiffService portalDiffService,
      PortalPublishingService portalPublishingService) {
    this.authUtilService = authUtilService;
    this.portalDiffService = portalDiffService;
    this.portalPublishingService = portalPublishingService;
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
    return portalPublishingService.applyChanges(portalShortcode, destEnv, change, user);
  }
}
