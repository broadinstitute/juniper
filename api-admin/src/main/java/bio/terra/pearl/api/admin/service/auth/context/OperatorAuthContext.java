package bio.terra.pearl.api.admin.service.auth.context;

import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
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

  public DataAuditInfo dataAuditInfo() {
    return DataAuditInfo.builder().responsibleAdminUserId(getOperator().getId()).build();
  }
}
