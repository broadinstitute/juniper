package bio.terra.pearl.api.admin.service;

import bio.terra.pearl.api.admin.config.B2CConfiguration;
import bio.terra.pearl.core.shared.ApplicationRoutingPaths;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ConfigExtService {
  private B2CConfiguration b2CConfiguration;
  private ApplicationRoutingPaths applicationRoutingPaths;

  private Map<String, String> configMap;

  public ConfigExtService(
      B2CConfiguration b2CConfiguration, ApplicationRoutingPaths applicationRoutingPaths) {
    this.b2CConfiguration = b2CConfiguration;
    this.applicationRoutingPaths = applicationRoutingPaths;
    configMap = buildConfigMap();
  }

  public Map<String, String> getConfigMap() {
    return configMap;
  }

  private Map<String, String> buildConfigMap() {
    return Map.of(
        "b2cTenantName", b2CConfiguration.tenantName(),
        "b2cClientId", b2CConfiguration.clientId(),
        "b2cPolicyName", b2CConfiguration.policyName(),
        "participantUiHostname", applicationRoutingPaths.getParticipantUiHostname(),
        "participantApiHostname", applicationRoutingPaths.getParticipantApiHostname(),
        "adminUiHostname", applicationRoutingPaths.getAdminUiHostname(),
        "adminApiHostname", applicationRoutingPaths.getAdminApiHostname());
  }
}
