package bio.terra.pearl.core.service.publishing;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.model.portal.PortalEnvironmentConfig;
import bio.terra.pearl.core.model.publishing.ConfigChange;
import java.util.List;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class PortalUpdateServiceTests extends BaseSpringBootTest {
    @Autowired
    private PortalUpdateService portalUpdateService;


    @Test
    public void testApplyConfigChanges() throws Exception {
        ConfigChange change = new ConfigChange("password", (Object) "foo", (Object)"bar");
        PortalEnvironmentConfig config = PortalEnvironmentConfig.builder()
                .password("foo").build();
        portalUpdateService.applyChangesToEnvConfig(config, List.of(change));
        assertThat(config.getPassword(), equalTo("bar"));
    }
}
