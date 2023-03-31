package bio.terra.pearl.core.shared;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.portal.PortalEnvironmentFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.portal.PortalEnvironmentConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ApplicationRoutingPathsTests extends BaseSpringBootTest {
    @Autowired
    private ApplicationRoutingPaths routingPaths;
    @Autowired
    private PortalEnvironmentFactory portalEnvironmentFactory;

    @Test
    public void testBaseUrlLive() {
        PortalEnvironmentConfig portalEnvironmentConfig = PortalEnvironmentConfig.builder()
                .participantHostname("newstudy.org")
                .build();
        PortalEnvironment portalEnv = portalEnvironmentFactory.builder("testDashLinkHostnamesIrb")
                .portalEnvironmentConfig(portalEnvironmentConfig).environmentName(EnvironmentName.live).build();
        String result = routingPaths.getParticipantBaseUrl(portalEnv, "whatevs");
        assertThat(result, equalTo("https://newstudy.org"));
    }

    @Test
    public void testBaseUrlIrb() {
        PortalEnvironmentConfig portalEnvironmentConfig = PortalEnvironmentConfig.builder()
                .participantHostname("newstudy.org")
                .build();
        PortalEnvironment portalEnv = portalEnvironmentFactory.builder("testDashLinkHostnamesIrb")
                .portalEnvironmentConfig(portalEnvironmentConfig).environmentName(EnvironmentName.irb).build();
        String result = routingPaths.getParticipantBaseUrl(portalEnv, "whatevs");
        assertThat(result, equalTo("https://irb.newstudy.org"));
    }

    @Test
    public void testBaseUrlLocal() {
        PortalEnvironmentConfig portalEnvironmentConfig = PortalEnvironmentConfig.builder()
                .participantHostname(null)
                .build();
        PortalEnvironment portalEnv = portalEnvironmentFactory.builder("testDashLinkHostnamesLocalhost")
                .portalEnvironmentConfig(portalEnvironmentConfig).environmentName(EnvironmentName.sandbox).build();
        String result = routingPaths.getParticipantBaseUrl(portalEnv, "snazzportal");
        assertThat(result, equalTo("https://sandbox.snazzportal.localhost:3001"));
    }
}
