package bio.terra.pearl.api.admin.service.auth;

import bio.terra.pearl.api.admin.service.auth.context.PortalEnrolleeAuthContext;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.study.PortalStudy;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import java.util.UUID;
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
public class EnforcePortalEnrolleePermissionAspect
    extends BaseEnforcePermissionAspect<
        PortalEnrolleeAuthContext, EnforcePortalEnrolleePermission> {
  private final AuthUtilService authUtilService;
  private final StudyEnvironmentService studyEnvironmentService;
  private final EnrolleeService enrolleeService;

  public EnforcePortalEnrolleePermissionAspect(
      AuthUtilService authUtilService,
      StudyEnvironmentService studyEnvironmentService,
      EnrolleeService enrolleeService) {
    this.authUtilService = authUtilService;
    this.studyEnvironmentService = studyEnvironmentService;
    this.enrolleeService = enrolleeService;
  }

  @Around(value = "@annotation(EnforcePortalEnrolleePermission)")
  public Object enforcePermission(ProceedingJoinPoint joinPoint) throws Throwable {
    PortalEnrolleeAuthContext authContext = extractAuthContext(joinPoint);
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
    String enrolleeShortcode = authContext.getEnrolleeShortcodeOrId();
    if (enrolleeShortcode != null && enrolleeShortcode.length() > 16) {
      // it's an id, not a shortcode
      enrolleeShortcode =
          enrolleeService
              .find(UUID.fromString(enrolleeShortcode))
              .orElseThrow(() -> new NotFoundException("Enrollee not found"))
              .getShortcode();
    }
    Enrollee enrollee =
        enrolleeService
            .findByShortcodeAndStudyEnvId(enrolleeShortcode, studyEnv.getId())
            .orElseThrow(() -> new NotFoundException("Enrollee not found"));

    authContext.setEnrollee(enrollee);

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
  protected Class<PortalEnrolleeAuthContext> getAuthContextClass() {
    return PortalEnrolleeAuthContext.class;
  }

  @Override
  protected Class<EnforcePortalEnrolleePermission> getAnnotationClass() {
    return EnforcePortalEnrolleePermission.class;
  }
}
