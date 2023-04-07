package bio.terra.pearl.core.factory.portal;

import bio.terra.pearl.core.factory.EnvironmentFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.portal.PortalEnvironmentConfig;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PortalEnvironmentFactory {
    @Autowired
    private EnvironmentFactory environmentFactory;
    @Autowired
    private PortalFactory portalFactory;
    @Autowired
    private PortalEnvironmentService portalEnvironmentService;

    public PortalEnvironment.PortalEnvironmentBuilder builder(String testName) {
        EnvironmentName envName = EnvironmentName.values()[RandomUtils.nextInt(0, 3)];
        return PortalEnvironment.builder()
                .environmentName(envName);
    }

    public PortalEnvironment.PortalEnvironmentBuilder builderWithDependencies(String testName, EnvironmentName envName) {
        Portal portal = portalFactory.buildPersisted(testName);
        environmentFactory.buildPersisted(testName, envName);
        return builder(testName)
                .portalId(portal.getId())
                .portalEnvironmentConfig(new PortalEnvironmentConfig())
                .environmentName(envName);
    }

    public PortalEnvironment.PortalEnvironmentBuilder builderWithDependencies(String testName) {
        Portal portal = portalFactory.buildPersisted(testName);
        return builder(testName)
                .portalId(portal.getId())
                .portalEnvironmentConfig(new PortalEnvironmentConfig())
                .environmentName(environmentFactory.buildPersisted(testName).getName());
    }

    public PortalEnvironment buildPersisted(String testName) {
        return portalEnvironmentService.create(builderWithDependencies(testName).build());
    }

    public PortalEnvironment buildPersisted(String testName, EnvironmentName envName) {
        return portalEnvironmentService.create(builderWithDependencies(testName, envName).build());
    }
}
