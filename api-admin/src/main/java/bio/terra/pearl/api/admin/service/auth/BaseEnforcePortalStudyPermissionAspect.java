package bio.terra.pearl.api.admin.service.auth;

import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.study.PortalStudy;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

/** Aspect to enforce portal permissions on methods annotated with EnforcePortalPermission. */
@Component
@Aspect
@Slf4j
public class BaseEnforcePortalStudyPermissionAspect
    extends BaseEnforcePermissionAspect<PortalStudyAuthContext, EnforcePortalStudyPermission> {
  private final AuthUtilService authUtilService;

  public BaseEnforcePortalStudyPermissionAspect(AuthUtilService authUtilService) {
    this.authUtilService = authUtilService;
  }

  @Around(value = "@annotation(EnforcePortalStudyPermission)")
  public Object enforcePermission(ProceedingJoinPoint joinPoint) throws Throwable {
    PortalStudyAuthContext authContext = extractAuthContext(joinPoint);
    String permission = getPermissionName(joinPoint);
    Portal portal =
        authUtilService.authUserToPortalWithPermission(
            authContext.getOperator(), authContext.getPortalShortcode(), permission);
    authContext.setPortal(portal);

    PortalStudy portalStudy =
        authUtilService.authUserToStudy(
            authContext.getOperator(),
            authContext.getPortalShortcode(),
            authContext.getStudyShortcode());
    authContext.setPortalStudy(portalStudy);
    return joinPoint.proceed();
  }

  @Override
  protected Class<PortalStudyAuthContext> getAuthContextClass() {
    return PortalStudyAuthContext.class;
  }

  @Override
  protected String getPermissionName(ProceedingJoinPoint joinPoint) {
    return ((MethodSignature) joinPoint.getSignature())
        .getMethod()
        .getAnnotation(getAnnotationClass())
        .permission();
  }

  @Override
  protected Class<EnforcePortalStudyPermission> getAnnotationClass() {
    return EnforcePortalStudyPermission.class;
  }
}
