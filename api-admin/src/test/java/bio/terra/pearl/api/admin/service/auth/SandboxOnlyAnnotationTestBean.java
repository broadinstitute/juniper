package bio.terra.pearl.api.admin.service.auth;

import bio.terra.pearl.api.admin.service.auth.context.EnvironmentAwareAuthContext;
import org.springframework.stereotype.Component;

@Component
public class SandboxOnlyAnnotationTestBean {
  @SandboxOnly
  public void sandboxOnlyMethod(EnvironmentAwareAuthContext authContext) {}

  public void allEnvMethod(EnvironmentAwareAuthContext authContext) {}

  @SandboxOnly
  public void sandboxOnlyMethodNoAuthContext(String foo) {}
}
