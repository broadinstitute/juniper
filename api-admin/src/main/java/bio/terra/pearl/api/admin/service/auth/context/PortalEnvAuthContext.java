package bio.terra.pearl.api.admin.service.auth.context;

import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.study.PortalStudy;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@Setter
public class PortalEnvAuthContext extends PortalAuthContext implements EnvironmentAwareAuthContext {
  private PortalStudy portalStudy;
  private EnvironmentName environmentName;

  public static PortalEnvAuthContext of(
      AdminUser operator, String portalShortcode, EnvironmentName environmentName) {
    return PortalEnvAuthContext.builder()
        .operator(operator)
        .portalShortcode(portalShortcode)
        .environmentName(environmentName)
        .build();
  }
}
