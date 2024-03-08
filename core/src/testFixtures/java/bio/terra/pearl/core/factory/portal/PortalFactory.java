package bio.terra.pearl.core.factory.portal;

import bio.terra.pearl.core.factory.EnvironmentFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.portal.PortalEnvironmentConfig;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import bio.terra.pearl.core.service.portal.PortalService;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PortalFactory {
    @Autowired
    private PortalService portalService;
    @Autowired
    private PortalEnvironmentService portalEnvironmentService;
    @Autowired
    private EnvironmentFactory environmentFactory;

    public Portal.PortalBuilder builder(String testName) {
        return Portal.builder()
                .name(testName + RandomStringUtils.randomAlphabetic(6))
                .shortcode(RandomStringUtils.randomAlphabetic(7));
    }

    public Portal.PortalBuilder builderWithDependencies(String testName) {
        return builder(testName);
    }

    public Portal buildPersisted(String testName) {
        return portalService.create(builderWithDependencies(testName).build());
    }

    public Portal buildPersistedWithEnvironments(String testName) {
        Portal portal = this.buildPersisted(testName);
        for (EnvironmentName envName : EnvironmentName.values()) {
            environmentFactory.buildPersisted(testName, envName);
            portalEnvironmentService.create(PortalEnvironment.builder()
                    .portalId(portal.getId())
                    .portalEnvironmentConfig(new PortalEnvironmentConfig())
                    .environmentName(envName)
                    .build());
        }
        return portal;
    }
}
