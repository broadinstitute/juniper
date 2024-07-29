package bio.terra.pearl.api.admin.service.auth;

import bio.terra.pearl.api.admin.service.auth.context.OperatorAuthContext;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Component
@Aspect
@Slf4j
public class SuperuserOnlyAspect {
  @Before(value = "@annotation(SuperuserOnly)")
  public void enforceSuperuserOnly(JoinPoint joinPoint) {
    OperatorAuthContext authContext =
        BaseEnforcePermissionAspect.extractAuthContext(
            joinPoint, OperatorAuthContext.class, "SuperuserOnly");
    if (!authContext.getOperator().isSuperuser()) {
      throw new PermissionDeniedException("You do not have permission for this operation");
    }
  }
}
