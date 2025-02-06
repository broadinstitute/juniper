package bio.terra.pearl.api.admin.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

import bio.terra.pearl.api.admin.AuthAnnotationSpec;
import bio.terra.pearl.api.admin.AuthTestUtils;
import bio.terra.pearl.api.admin.config.B2CConfiguration;
import bio.terra.pearl.api.admin.service.auth.SuperuserOnly;
import bio.terra.pearl.api.admin.service.auth.context.OperatorAuthContext;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.service.address.AddressValidationConfig;
import bio.terra.pearl.core.service.export.integration.AirtableExporter;
import bio.terra.pearl.core.service.kit.pepper.LivePepperDSMClient;
import bio.terra.pearl.core.service.logging.MixpanelService;
import bio.terra.pearl.core.shared.ApplicationRoutingPaths;
import java.util.List;
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
  @MockBean private AddressValidationConfig addressValidationConfig;
  @MockBean private AirtableExporter.AirtableConfig airtableConfig;
  @MockBean private MixpanelService.MixpanelConfig mixpanelConfig;

  @Test
  public void testAllMethodsAnnotated() {
    AuthTestUtils.assertAllMethodsAnnotated(
        new ConfigExtService(
            b2CConfiguration,
            applicationRoutingPaths,
            pepperDSMConfig,
            addressValidationConfig,
            airtableConfig,
            mixpanelConfig),
        Map.of(
            "maskSecret",
            AuthAnnotationSpec.withPublicAnnotation(),
            "getConfigMap",
            AuthAnnotationSpec.withPublicAnnotation(),
            "getInternalConfigMap",
            AuthAnnotationSpec.withOtherAnnotations(List.of(SuperuserOnly.class))));
  }

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
        new ConfigExtService(
            b2CConfiguration,
            applicationRoutingPaths,
            pepperDSMConfig,
            addressValidationConfig,
            airtableConfig,
            mixpanelConfig);
    Map<String, String> configMap = configExtService.getConfigMap();
    Assertions.assertEquals("something.org", configMap.get("participantUiHostname"));
  }

  @Test
  public void testInternalConfigMap() {
    AdminUser user = AdminUser.builder().superuser(true).build();
    MockEnvironment mockEnvironment =
        new MockEnvironment()
            .withProperty("env.dsm.basePath", "basePath1")
            .withProperty("env.dsm.issuerClaim", "issuerClaim1")
            .withProperty("env.dsm.secret", "superSecret")
            .withProperty("env.addrValidation.addrValidationServiceClass", "someClass")
            .withProperty("env.addrValidation.smartyAuthId", "sm_id")
            .withProperty("env.addrValidation.smartyAuthToken", "sm_token")
            .withProperty("env.mixpanel.enabled", "true")
            .withProperty("env.mixpanel.token", "mp_token");

    LivePepperDSMClient.PepperDSMConfig testPepperConfig =
        new LivePepperDSMClient.PepperDSMConfig(mockEnvironment);

    AddressValidationConfig testAddrConfig = new AddressValidationConfig(mockEnvironment);
    MixpanelService.MixpanelConfig testMixpanelConfig =
        new MixpanelService.MixpanelConfig(mockEnvironment);
    ConfigExtService configExtService =
        new ConfigExtService(
            b2CConfiguration,
            applicationRoutingPaths,
            testPepperConfig,
            testAddrConfig,
            airtableConfig,
            testMixpanelConfig);
    @SuppressWarnings("unchecked")
    Map<String, ?> dsmConfigMap =
        (Map<String, ?>)
            configExtService
                .getInternalConfigMap(OperatorAuthContext.of(user))
                .get("pepperDsmConfig");
    @SuppressWarnings("unchecked")
    Map<String, ?> addressValidationConfigMap =
        (Map<String, ?>)
            configExtService
                .getInternalConfigMap(OperatorAuthContext.of(user))
                .get("addrValidationConfig");
    assertThat(dsmConfigMap.get("basePath"), equalTo("basePath1"));
    assertThat(dsmConfigMap.get("issuerClaim"), equalTo("issuerClaim1"));
    assertThat(dsmConfigMap.get("secret"), equalTo("su..."));

    assertThat(addressValidationConfigMap.get("addrValidationServiceClass"), equalTo("someClass"));
    assertThat(addressValidationConfigMap.get("smartyAuthId"), equalTo("sm_id"));
    assertThat(addressValidationConfigMap.get("smartyAuthToken"), equalTo("sm..."));

    assertThat(testMixpanelConfig.getEnabled(), equalTo(true));
    assertThat(testMixpanelConfig.getToken(), equalTo("mp_token"));
  }

  @Test
  public void testSecretMask() {
    assertThat(ConfigExtService.maskSecret(""), equalTo(""));
    assertThat(ConfigExtService.maskSecret("shortSecret"), equalTo("sh..."));
    assertThat(
        ConfigExtService.maskSecret("veryloooooooooooooooooongsecret"), equalTo("ver...ret"));
  }
}
