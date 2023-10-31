package bio.terra.pearl.api.participant.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

@Slf4j
@Service
public class B2CConfigurationService {

  @Getter private B2CConfiguration b2cConfiguration;
  private final Map<String, Map<String, String>> portalToConfig = new HashMap<>();

  public B2CConfigurationService() {
    b2cConfiguration = new B2CConfiguration();
  }

  /**
   * Get the B2C configuration for a portal. Returns an empty map if portal config for portal is not
   * specified
   */
  public Map<String, String> getB2CForPortal(String portalShortcode) {
    // see if already constructed
    Map<String, String> portalConfig = portalToConfig.get(portalShortcode);
    if (portalConfig == null) {
      portalConfig = buildConfigMap(portalShortcode);
    }

    return portalConfig;
  }

  /**
   * Initialize B2C configuration from a yaml file. The file can be specified as an absolute path or
   * a relative path. If relative, it is loaded from the classpath.
   */
  public void initB2CConfig(String b2cConfigFile) {
    if (StringUtils.isBlank(b2cConfigFile)) {
      log.error("b2c-config-file property is not set");
      return;
    }

    Yaml yaml = new Yaml(new Constructor(B2CConfiguration.class, new LoaderOptions()));

    File file = new File(b2cConfigFile);
    if (file.isAbsolute()) {
      // absolute path, load from file system
      if (!file.exists()) {
        log.error(
            "b2c-config-file property is set to an absolute path that does not exist: {}",
            b2cConfigFile);
        return;
      }

      try (InputStream str = new FileInputStream(file)) {
        b2cConfiguration = yaml.load(str);
      } catch (Exception e) {
        log.error("Error loading b2c config file: {}", b2cConfigFile, e);
      }
    } else {
      // relative path, load from classpath
      try (InputStream str =
          Thread.currentThread().getContextClassLoader().getResourceAsStream(b2cConfigFile)) {
        b2cConfiguration = yaml.load(str);
      } catch (Exception e) {
        log.error("Error loading b2c config file: {}", b2cConfigFile, e);
      }
    }
  }

  protected Map<String, String> buildConfigMap(String portalShortcode) {
    B2CConfiguration.B2CProperties b2cConfig =
        getB2cConfiguration().getPortalB2CProperties(portalShortcode);
    if (b2cConfig == null) {
      return Collections.emptyMap();
    }
    Map<String, String> config =
        Map.of(
            "b2cTenantName",
            StringUtils.defaultIfEmpty(b2cConfig.getTenantName(), ""),
            "b2cClientId",
            StringUtils.defaultIfEmpty(b2cConfig.getClientId(), ""),
            "b2cPolicyName",
            StringUtils.defaultIfEmpty(b2cConfig.getPolicyName(), ""),
            "b2cChangePasswordPolicyName",
            StringUtils.defaultIfEmpty(b2cConfig.getChangePasswordPolicyName(), ""));
    portalToConfig.put(portalShortcode, config);
    return config;
  }
}
