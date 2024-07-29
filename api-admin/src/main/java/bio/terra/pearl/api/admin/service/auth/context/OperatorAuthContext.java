package bio.terra.pearl.api.admin.service.auth.context;

import bio.terra.pearl.core.model.admin.AdminUser;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@Setter
public class OperatorAuthContext {
  private AdminUser operator;

  public static OperatorAuthContext of(AdminUser operator) {
    return OperatorAuthContext.builder().operator(operator).build();
  }
}
