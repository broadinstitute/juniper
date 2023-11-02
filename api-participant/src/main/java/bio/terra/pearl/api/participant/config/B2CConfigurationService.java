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
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

@Slf4j
@Service
public class B2CConfigurationService {
  private final B2CConfiguration b2cConfiguration;
  @Getter private B2CPortalConfiguration portalConfiguration;
  private final Map<String, Map<String, String>> portalToConfig = new HashMap<>();

  public B2CConfigurationService(B2CConfiguration b2cConfiguration) {
    this.b2cConfiguration = b2cConfiguration;
    portalConfiguration = new B2CPortalConfiguration();
  }

  /** Get the B2C configuration for a portal. Returns null if B2C is not configured for portal */
  public Map<String, String> getB2CForPortal(String portalShortcode) {
    return portalToConfig.get(portalShortcode);
  }

  /**
   * Initialize B2C configuration from a yaml file. The file can be specified as an absolute path or
   * a relative path. If relative, it is loaded from the classpath.
   */
  @EventListener(ApplicationReadyEvent.class)
  public void initB2CConfig() {
    String b2cConfigFile = b2cConfiguration.configFile();
    if (StringUtils.isBlank(b2cConfigFile)) {
      log.error("b2c-config-file property is not set");
      return;
    }

    log.info("b2c-config-file = '{}'", b2cConfigFile);
    Yaml yaml = new Yaml(new Constructor(B2CPortalConfiguration.class, new LoaderOptions()));

    // for deployments into k8s, the config file is mounted into the container as a volume
    // for local deployments, the config file is on the classpath, at least for now
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
        portalConfiguration = yaml.load(str);
      } catch (Exception e) {
        log.error("Error loading b2c config file: {}", b2cConfigFile, e);
        return;
      }
    } else {
      // relative path, load from classpath
      ClassPathResource cpr = new ClassPathResource(b2cConfigFile);
      try (InputStream str = cpr.getInputStream()) {
        portalConfiguration = yaml.load(str);
      } catch (Exception e) {
        log.error("Error loading b2c config file: {}", b2cConfigFile, e);
        return;
      }
    }
    buildPortalToConfig();
  }

  protected void buildPortalToConfig() {
    portalToConfig.clear();
    for (String portal : portalConfiguration.getB2CProperties().keySet()) {
      portalToConfig.put(portal, buildConfigMap(portal));
    }
  }

  protected Map<String, String> buildConfigMap(String portalShortcode) {
    B2CPortalConfiguration.B2CProperties b2cConfig =
        getPortalConfiguration().getPortalProperties(portalShortcode);
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
