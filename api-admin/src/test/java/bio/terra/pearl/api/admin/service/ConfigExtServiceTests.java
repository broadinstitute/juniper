package bio.terra.pearl.api.admin.service;

import bio.terra.pearl.api.admin.config.B2CConfiguration;
import bio.terra.pearl.core.shared.ApplicationRoutingPaths;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = ConfigExtService.class)
public class ConfigExtServiceTests {
    @MockBean
    private ApplicationRoutingPaths applicationRoutingPaths;

    @MockBean
    private B2CConfiguration b2CConfiguration;

    @Test
    public void testConfigMap() {
        ConfigExtService configExtService = new ConfigExtService(b2CConfiguration, applicationRoutingPaths);
        when(statusService.getCurrentStatus()).thenReturn(systemStatus);
    }
}
