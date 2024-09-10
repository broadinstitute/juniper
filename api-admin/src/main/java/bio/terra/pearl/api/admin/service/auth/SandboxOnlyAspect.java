package bio.terra.pearl.api.admin.service.auth;

import bio.terra.pearl.api.admin.service.auth.context.EnvironmentAwareAuthContext;
import bio.terra.pearl.core.model.EnvironmentName;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class SandboxOnlyAspect {
  @Before(value = "@annotation(SandboxOnly)")
  public void enforceSandboxOnly(JoinPoint joinPoint) {
    EnvironmentAwareAuthContext authContext =
        BaseEnforcePermissionAspect.extractAuthContext(
            joinPoint, EnvironmentAwareAuthContext.class, "SandboxOnly");
    if (authContext.getEnvironmentName() != EnvironmentName.sandbox) {
      throw new UnsupportedOperationException(
          "This operation is only allowed in the sandbox environment");
    }
  }
}
