package bio.terra.pearl.api.admin.service.auth;

import bio.terra.pearl.api.admin.service.auth.context.OperatorAuthContext;
import org.springframework.stereotype.Component;

@Component
public class SuperuserOnlyTestBean {
  @SuperuserOnly
  public void superuserOnlyMethod(OperatorAuthContext authContext) {}

  public void anyUserMethod(OperatorAuthContext authContext) {}

  @SuperuserOnly
  public void superuserOnlyMethodNoAuthContext(String foo) {}
}
