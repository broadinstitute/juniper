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

import java.util.UUID;

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

    public PortalEnvironment.PortalEnvironmentBuilder builderWithDependencies(String testName, EnvironmentName envName, UUID portalId) {
        environmentFactory.buildPersisted(testName, envName);
        return builder(testName)
                .portalId(portalId)
                .portalEnvironmentConfig(new PortalEnvironmentConfig())
                .environmentName(envName);
    }

    public PortalEnvironment.PortalEnvironmentBuilder builderWithDependencies(String testName, EnvironmentName envName) {
        Portal portal = portalFactory.buildPersisted(testName);
        return builderWithDependencies(testName, envName, portal.getId());
    }

    public PortalEnvironment.PortalEnvironmentBuilder builderWithDependencies(String testName) {
        return builderWithDependencies(testName, EnvironmentName.values()[RandomUtils.nextInt(0, 3)]);
    }

    public PortalEnvironment buildPersisted(String testName) {
        return buildPersisted(testName, EnvironmentName.values()[RandomUtils.nextInt(0, 3)]);
    }

    public PortalEnvironment buildPersisted(String testName, EnvironmentName envName) {
        return portalEnvironmentService.create(builderWithDependencies(testName, envName).build());
    }

    public PortalEnvironment buildPersisted(String testName, EnvironmentName envName, UUID portalId) {
        return portalEnvironmentService.create(builderWithDependencies(testName, envName, portalId).build());
    }
}
