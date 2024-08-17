package bio.terra.pearl.api.admin.service.auth;

import bio.terra.pearl.api.admin.service.auth.context.OperatorAuthContext;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class AnyAdminUserAspect {
  @Before(value = "@annotation(SandboxOnly)")
  public void enforceOperatorRequired(JoinPoint joinPoint) {
    OperatorAuthContext authContext =
        BaseEnforcePermissionAspect.extractAuthContext(
            joinPoint, OperatorAuthContext.class, "AnyAdminUser");
    if (authContext.getOperator() == null) {
      throw new PermissionDeniedException("Signed-in user is required for this operation");
    }
  }
}
