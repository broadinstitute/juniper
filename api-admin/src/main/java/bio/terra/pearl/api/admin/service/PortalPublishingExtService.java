package bio.terra.pearl.api.admin.service;

import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.publishing.PortalEnvironmentChange;
import bio.terra.pearl.core.service.publishing.PortalPublishingService;
import org.springframework.stereotype.Service;

@Service
public class PortalPublishingExtService {
    private AuthUtilService authUtilService;
    private PortalPublishingService portalPublishingService;

    public PortalPublishingExtService(AuthUtilService authUtilService,
                                      PortalPublishingService portalPublishingService) {
        this.authUtilService = authUtilService;
        this.portalPublishingService = portalPublishingService;
    }

    public PortalEnvironmentChange diff(String portalShortcode, EnvironmentName destEnv,
                                        EnvironmentName sourceEnv, AdminUser user) {
        authUtilService.authUserToPortal(user, portalShortcode);
        try {
            return portalPublishingService.diffPortalEnvs(portalShortcode, destEnv, sourceEnv);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public PortalEnvironmentChange update(String portalShortcode, EnvironmentName destEnv,
                                          EnvironmentName sourceEnv,
                                          PortalEnvironmentChange change, AdminUser user) {
        authUtilService.authUserToPortal(user, portalShortcode);
        try {
            return portalPublishingService.update(portalShortcode, sourceEnv, destEnv, change, user);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}
