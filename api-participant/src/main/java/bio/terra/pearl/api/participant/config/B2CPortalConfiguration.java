package bio.terra.pearl.api.participant.config;

import java.util.Map;
import lombok.Data;
import lombok.Setter;

@Setter
public class B2CPortalConfiguration {
  private Map<String, B2CProperties> b2c;

  public B2CPortalConfiguration() {
    createDefaultProperties();
  }

  protected void createDefaultProperties() {
    b2c = Map.of("missingB2CProperties", new B2CProperties());
  }

  public B2CPortalConfiguration.B2CProperties getPortalProperties(String portal) {
    return b2c.get(portal);
  }

  Map<String, B2CPortalConfiguration.B2CProperties> getB2CProperties() {
    return b2c;
  }

  @Data
  public static class B2CProperties {
    private String tenantName;
    private String clientId;
    private String policyName;
    private String changePasswordPolicyName;
  }
}
