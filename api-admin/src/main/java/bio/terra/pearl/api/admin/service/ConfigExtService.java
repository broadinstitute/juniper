package bio.terra.pearl.api.admin.service;

import bio.terra.pearl.api.admin.config.B2CConfiguration;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.service.address.AddressValidationConfig;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import bio.terra.pearl.core.service.export.integration.AirtableExporter;
import bio.terra.pearl.core.service.kit.pepper.LivePepperDSMClient;
import bio.terra.pearl.core.shared.ApplicationRoutingPaths;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class ConfigExtService {
  private B2CConfiguration b2CConfiguration;
  private ApplicationRoutingPaths applicationRoutingPaths;

  private Map<String, String> configMap;
  private final LivePepperDSMClient.PepperDSMConfig pepperDSMConfig;
  private final AddressValidationConfig addressValidationConfig;
  private final AirtableExporter.AirtableConfig airtableConfig;

  public ConfigExtService(
      B2CConfiguration b2CConfiguration,
      ApplicationRoutingPaths applicationRoutingPaths,
      LivePepperDSMClient.PepperDSMConfig pepperDSMConfig,
      AddressValidationConfig addressValidationConfig,
      AirtableExporter.AirtableConfig airtableConfig) {
    this.b2CConfiguration = b2CConfiguration;
    this.pepperDSMConfig = pepperDSMConfig;
    this.applicationRoutingPaths = applicationRoutingPaths;
    this.addressValidationConfig = addressValidationConfig;
    this.airtableConfig = airtableConfig;

    configMap = buildConfigMap();
  }

  public Map<String, String> getConfigMap() {
    // no auth needed -- the config is all public information sent to the frontend
    return configMap;
  }

  private Map<String, String> buildConfigMap() {
    return Map.of(
        "b2cTenantName",
        StringUtils.defaultIfEmpty(b2CConfiguration.tenantName(), ""),
        "b2cClientId",
        StringUtils.defaultIfEmpty(b2CConfiguration.clientId(), ""),
        "b2cPolicyName",
        StringUtils.defaultIfEmpty(b2CConfiguration.policyName(), ""),
        "participantUiHostname",
        StringUtils.defaultIfEmpty(applicationRoutingPaths.getParticipantUiHostname(), ""),
        "participantApiHostname",
        StringUtils.defaultIfEmpty(applicationRoutingPaths.getParticipantApiHostname(), ""),
        "adminUiHostname",
        StringUtils.defaultIfEmpty(applicationRoutingPaths.getAdminUiHostname(), ""),
        "adminApiHostname",
        StringUtils.defaultIfEmpty(applicationRoutingPaths.getAdminApiHostname(), ""),
        "deploymentZone",
        StringUtils.defaultIfEmpty(applicationRoutingPaths.getDeploymentZone(), ""));
  }

  /**
   * returns non-public configuration information -- note that this still should not return actual
   * secrets
   */
  public Map<String, ?> getInternalConfigMap(AdminUser user) {
    if (!user.isSuperuser()) {
      throw new PermissionDeniedException("You do not have permission to view this config");
    }
    Map<String, Map<String, String>> internalConfigMap =
        Map.of(
            "pepperDsmConfig",
            Map.of(
                "secret",
                maskSecret(pepperDSMConfig.getSecret()),
                "issuerClaim",
                pepperDSMConfig.getIssuerClaim(),
                "basePath",
                pepperDSMConfig.getBasePath()),
            "addrValidationConfig",
            Map.of(
                "addrValidationServiceClass", addressValidationConfig.getAddressValidationClass(),
                "smartyAuthId", addressValidationConfig.getAuthId(),
                "smartyAuthToken", maskSecret(addressValidationConfig.getAuthToken())),
            "airtable",
            Map.of("authToken", maskSecret(airtableConfig.getAuthToken())));
    return internalConfigMap;
  }

  public static String maskSecret(String secret) {
    if (StringUtils.isBlank(secret)) {
      return "";
    }
    if (secret.length() < 15) {
      return secret.substring(0, 2) + "...";
    }
    return secret.substring(0, 3) + "..." + secret.substring(secret.length() - 3, secret.length());
  }
}
