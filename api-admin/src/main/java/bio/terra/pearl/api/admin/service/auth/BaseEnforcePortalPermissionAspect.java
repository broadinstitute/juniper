package bio.terra.pearl.api.admin.service.auth;

import bio.terra.pearl.api.admin.service.auth.context.PortalAuthContext;
import bio.terra.pearl.core.model.portal.Portal;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Component
@Aspect
@Slf4j
/**
 * Aspect to enforce portal permissions on methods annotated with EnforcePortalPermission. Inspired
 * by https://stackoverflow.com/questions/50882444/can-spring-annotation-access-method-parameters
 */
public class BaseEnforcePortalPermissionAspect
    extends BaseEnforcePermissionAspect<PortalAuthContext, EnforcePortalPermission> {

  private final AuthUtilService authUtilService;

  public BaseEnforcePortalPermissionAspect(AuthUtilService authUtilService) {
    this.authUtilService = authUtilService;
  }

  @Around(value = "@annotation(EnforcePortalPermission)")
  public Object enforcePermission(ProceedingJoinPoint joinPoint) throws Throwable {
    PortalAuthContext authContext = extractAuthContext(joinPoint);
    String permission = getPermissionName(joinPoint);
    Portal portal =
        authUtilService.authUserToPortalWithPermission(
            authContext.getOperator(), authContext.getPortalShortcode(), permission);
    authContext.setPortal(portal);
    return joinPoint.proceed();
  }

  @Override
  protected String getPermissionName(ProceedingJoinPoint joinPoint) {
    return ((MethodSignature) joinPoint.getSignature())
        .getMethod()
        .getAnnotation(getAnnotationClass())
        .permission();
  }

  @Override
  protected Class<PortalAuthContext> getAuthContextClass() {
    return PortalAuthContext.class;
  }

  @Override
  protected Class<EnforcePortalPermission> getAnnotationClass() {
    return EnforcePortalPermission.class;
  }
}
