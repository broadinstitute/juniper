package bio.terra.pearl.api.admin.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

import bio.terra.pearl.api.admin.config.B2CConfiguration;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import bio.terra.pearl.core.service.kit.LivePepperDSMClient;
import bio.terra.pearl.core.shared.ApplicationRoutingPaths;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = ConfigExtService.class)
@WebMvcTest
public class ConfigExtServiceTests {
  @MockBean private ApplicationRoutingPaths applicationRoutingPaths;

  @MockBean private B2CConfiguration b2CConfiguration;
  @MockBean private LivePepperDSMClient.PepperDSMConfig pepperDSMConfig;

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
        new ConfigExtService(b2CConfiguration, applicationRoutingPaths, pepperDSMConfig);
    Map<String, String> configMap = configExtService.getConfigMap();
    Assertions.assertEquals("something.org", configMap.get("participantUiHostname"));
  }

  @Test
  public void testInternalConfigRequiresSuperuser() {
    AdminUser user = AdminUser.builder().superuser(false).build();
    ConfigExtService configExtService =
        new ConfigExtService(b2CConfiguration, applicationRoutingPaths, pepperDSMConfig);
    Assertions.assertThrows(
        PermissionDeniedException.class,
        () -> {
          configExtService.getInternalConfigMap(user);
        });
  }

  @Test
  public void testInternalConfigMap() {
    AdminUser user = AdminUser.builder().superuser(true).build();
    MockEnvironment mockEnvironment =
        new MockEnvironment()
            .withProperty("env.dsm.useLiveDsm", "false")
            .withProperty("env.dsm.basePath", "basePath1")
            .withProperty("env.dsm.issuerClaim", "issuerClaim1")
            .withProperty("env.dsm.secret", "superSecret");

    LivePepperDSMClient.PepperDSMConfig testConfig =
        new LivePepperDSMClient.PepperDSMConfig(mockEnvironment);
    ConfigExtService configExtService =
        new ConfigExtService(b2CConfiguration, applicationRoutingPaths, testConfig);
    Map<String, ?> dsmConfigMap =
        (Map<String, ?>) configExtService.getInternalConfigMap(user).get("pepperDsmConfig");
    assertThat(dsmConfigMap.get("basePath"), equalTo("basePath1"));
    assertThat(dsmConfigMap.get("issuerClaim"), equalTo("issuerClaim1"));
    assertThat(dsmConfigMap.get("secret"), equalTo("su..."));
    assertThat(dsmConfigMap.get("useLiveDsm"), equalTo(false));
  }

  @Test
  public void testSecretMask() {
    assertThat(ConfigExtService.maskSecret(""), equalTo(""));
    assertThat(ConfigExtService.maskSecret("shortSecret"), equalTo("sh..."));
    assertThat(
        ConfigExtService.maskSecret("veryloooooooooooooooooongsecret"), equalTo("ver...ret"));
  }
}
