package bio.terra.pearl.api.admin.service.auth;

import bio.terra.pearl.api.admin.service.auth.context.PortalStudyEnvAuthContext;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.study.PortalStudy;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

/**
 * Aspect to enforce portal permissions on methods annotated with EnforcePortalStudyEnvPermission.
 */
@Component
@Aspect
@Slf4j
public class BaseEnforcePortalStudyEnvPermissionAspect
    extends BaseEnforcePermissionAspect<
        PortalStudyEnvAuthContext, EnforcePortalStudyEnvPermission> {
  private final AuthUtilService authUtilService;
  private final StudyEnvironmentService studyEnvironmentService;

  public BaseEnforcePortalStudyEnvPermissionAspect(
      AuthUtilService authUtilService, StudyEnvironmentService studyEnvironmentService) {
    this.authUtilService = authUtilService;
    this.studyEnvironmentService = studyEnvironmentService;
  }

  @Around(value = "@annotation(EnforcePortalStudyEnvPermission)")
  public Object enforcePermission(ProceedingJoinPoint joinPoint) throws Throwable {
    PortalStudyEnvAuthContext authContext = extractAuthContext(joinPoint);
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

    StudyEnvironment studyEnv =
        studyEnvironmentService.verifyStudy(
            authContext.getStudyShortcode(), authContext.getEnvironmentName());
    authContext.setStudyEnvironment(studyEnv);
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
  protected Class<PortalStudyEnvAuthContext> getAuthContextClass() {
    return PortalStudyEnvAuthContext.class;
  }

  @Override
  protected Class<EnforcePortalStudyEnvPermission> getAnnotationClass() {
    return EnforcePortalStudyEnvPermission.class;
  }
}
