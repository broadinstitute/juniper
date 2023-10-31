package bio.terra.pearl.api.participant.config;

import static io.jsonwebtoken.lang.Assert.notNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.net.URL;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class B2CConfigurationServiceTest {

  @TempDir File tempDir;

  @Test
  void testInitB2CConfigRelativePath() {
    B2CConfigurationService b2CService =
        new B2CConfigurationService(new B2CConfiguration("b2c-config.yml"));
    b2CService.initB2CConfig();
    B2CPortalConfiguration b2cConfig = b2CService.getPortalConfiguration();
    notNull(b2cConfig, "b2cConfig should not be null");
    B2CPortalConfiguration.B2CProperties b2cProperties = b2cConfig.getPortalProperties("ourhealth");
    assertThat(b2cProperties.getTenantName(), equalTo("ddpdevb2c"));
  }

  @Test
  void testInitB2CConfigAbsPath() {
    // create a file that is not on the resource path
    File tempFile = null;
    try {
      tempFile = new File(tempDir, "b2c-config.yml");
      URL url = Thread.currentThread().getContextClassLoader().getResource("test-b2c-config.yml");
      FileUtils.copyFile(new File(url.getPath()), tempFile);
    } catch (Exception e) {
      fail("Failed to create b2c-config.yml", e);
    }

    // ensure initialization
    B2CConfigurationService b2CService =
        new B2CConfigurationService(new B2CConfiguration(tempFile.getPath()));
    b2CService.initB2CConfig();
    B2CPortalConfiguration b2cConfig = b2CService.getPortalConfiguration();
    notNull(b2cConfig, "b2cConfig should not be null");
    B2CPortalConfiguration.B2CProperties b2cProperties = b2cConfig.getPortalProperties("portal2");
    assertThat(b2cProperties.getTenantName(), equalTo("def"));

    // ensure config map building
    Map<String, String> configMap = b2CService.getB2CForPortal("portal2");
    assertThat(configMap.size(), equalTo(4));
    assertThat(configMap.get("b2cTenantName"), equalTo("def"));
    assertThat(
        configMap.get("b2cPolicyName"), equalTo("B2C_1B_ddp_participant_signup_signin_test"));
  }
}
