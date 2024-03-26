package bio.terra.pearl.core.shared;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.portal.PortalEnvironmentFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.portal.PortalEnvironmentConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ApplicationRoutingPathsTests extends BaseSpringBootTest {
    @Autowired
    private ApplicationRoutingPaths routingPaths;
    @Autowired
    private PortalEnvironmentFactory portalEnvironmentFactory;

    @Test
    public void testBaseUrlLive(TestInfo info) {
        PortalEnvironmentConfig portalEnvironmentConfig = PortalEnvironmentConfig.builder()
                .participantHostname("newstudy.org")
                .build();
        PortalEnvironment portalEnv = portalEnvironmentFactory.builder(getTestName(info))
                .portalEnvironmentConfig(portalEnvironmentConfig).environmentName(EnvironmentName.live).build();
        String result = routingPaths.getParticipantBaseUrl(portalEnv, portalEnvironmentConfig, "whatevs");
        assertThat(result, equalTo("https://newstudy.org"));
    }

    @Test
    public void testBaseUrlIrb(TestInfo info) {
        PortalEnvironmentConfig portalEnvironmentConfig = PortalEnvironmentConfig.builder()
                .participantHostname("newstudy.org")
                .build();
        PortalEnvironment portalEnv = portalEnvironmentFactory.builder(getTestName(info))
                .portalEnvironmentConfig(portalEnvironmentConfig).environmentName(EnvironmentName.irb).build();
        String result = routingPaths.getParticipantBaseUrl(portalEnv, portalEnvironmentConfig,"whatevs");
        assertThat(result, equalTo("https://irb.newstudy.org"));
    }

    @Test
    public void testBaseUrlLocal(TestInfo info) {
        PortalEnvironmentConfig portalEnvironmentConfig = PortalEnvironmentConfig.builder()
                .participantHostname(null)
                .build();
        PortalEnvironment portalEnv = portalEnvironmentFactory.builder(getTestName(info))
                .portalEnvironmentConfig(portalEnvironmentConfig).environmentName(EnvironmentName.sandbox).build();
        String result = routingPaths.getParticipantBaseUrl(portalEnv, portalEnvironmentConfig,"snazzportal");
        assertThat(result, equalTo("https://sandbox.snazzportal.localhost:3001"));
    }
}
