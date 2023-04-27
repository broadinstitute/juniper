package bio.terra.pearl.api.admin.service;

import static org.mockito.Mockito.when;

import bio.terra.pearl.api.admin.config.B2CConfiguration;
import bio.terra.pearl.core.shared.ApplicationRoutingPaths;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = ConfigExtService.class)
@WebMvcTest
public class ConfigExtServiceTests {
  @MockBean private ApplicationRoutingPaths applicationRoutingPaths;

  @MockBean private B2CConfiguration b2CConfiguration;

  @Test
  public void testConfigMap() {
    when(applicationRoutingPaths.getParticipantUiHostname()).thenReturn("something.org");
    when(applicationRoutingPaths.getParticipantApiHostname()).thenReturn("something1.org");
    when(applicationRoutingPaths.getAdminUiHostname()).thenReturn("admin.org");
    when(applicationRoutingPaths.getAdminApiHostname()).thenReturn("adminApi.org");
    when(b2CConfiguration.tenantName()).thenReturn("tenant123");
    when(b2CConfiguration.clientId()).thenReturn("client123");
    when(b2CConfiguration.policyName()).thenReturn("policy123");
    ConfigExtService configExtService =
        new ConfigExtService(b2CConfiguration, applicationRoutingPaths);
    Map<String, String> configMap = configExtService.getConfigMap();
    Assertions.assertEquals("something.org", configMap.get("participantUiHostname"));
  }
}
