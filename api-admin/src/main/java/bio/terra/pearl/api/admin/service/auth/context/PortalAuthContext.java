package bio.terra.pearl.api.admin.service.auth.context;

import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.portal.Portal;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/** collection of objects we've authenticated against */
@Getter
@Setter
@SuperBuilder
public class PortalAuthContext extends OperatorAuthContext {
  private String portalShortcode;
  private Portal portal;

  public static PortalAuthContext of(AdminUser operator, String portalShortcode) {
    return PortalAuthContext.builder().portalShortcode(portalShortcode).operator(operator).build();
  }
}
