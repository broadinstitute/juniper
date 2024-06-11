package bio.terra.pearl.api.admin.service.auth;

import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
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
 * Aspect to enforce portal permissions on methods. Inspired by
 * https://stackoverflow.com/questions/50882444/can-spring-annotation-access-method-parameters
 */
public class PortalPermissionEnforcer {
  private static final String ADMIN_USER_USAGE_ERROR =
      "EnforcePortalPermission annotation must be used on a method whose last argument is the AdminUser";
  private static final String SHORTCODE_USAGE_ERROR =
      "EnforcePortalPermission annotation must be used on a method whose first argument is the Portal shortcode";
  private final AuthUtilService authUtilService;

  public PortalPermissionEnforcer(AuthUtilService authUtilService) {
    this.authUtilService = authUtilService;
  }

  @Around(value = "@annotation(EnforcePortalPermission)")
  public Object enforcePortalPermission(ProceedingJoinPoint joinPoint) throws Throwable {
    AdminUser operator = extractAdminUserOperator(joinPoint);
    String portalShortcode = extractPortalShortcode(joinPoint);
    EnforcePortalPermission epp =
        ((MethodSignature) joinPoint.getSignature())
            .getMethod()
            .getAnnotation(EnforcePortalPermission.class);
    String permission = epp.permission();
    authUtilService.authUserToPortalWithPermission(operator, portalShortcode, permission);
    return joinPoint.proceed();
  }

  /**
   * this gets the operator by assuming it's the last argument to the method. It's probably good to
   * enforce that the AdminUser is always the list arg for consistency's sake. However, we could
   * eventually get fancy and use a parameter annotation to detect it.
   */
  protected AdminUser extractAdminUserOperator(ProceedingJoinPoint joinPoint) {
    Object[] methodArgs = joinPoint.getArgs();
    if (methodArgs.length == 0) {
      throw new UnsupportedOperationException(ADMIN_USER_USAGE_ERROR);
    }
    Object adminUser = methodArgs[methodArgs.length - 1];
    if (adminUser == null) {
      throw new PermissionDeniedException("User not found");
    }
    if (adminUser instanceof AdminUser) {
      return (AdminUser) adminUser;
    } else {
      throw new UnsupportedOperationException(ADMIN_USER_USAGE_ERROR);
    }
  }

  /** gets the portal shortcode by assuming it's the first argument */
  protected String extractPortalShortcode(ProceedingJoinPoint joinPoint) {
    Object[] methodArgs = joinPoint.getArgs();
    if (methodArgs.length < 1) {
      throw new UnsupportedOperationException(SHORTCODE_USAGE_ERROR);
    }
    Object shortcode = methodArgs[0];
    if (shortcode == null) {
      throw new IllegalArgumentException("Portal shortcode not specified");
    }
    if (shortcode instanceof String) {
      return (String) shortcode;
    } else {
      throw new UnsupportedOperationException(SHORTCODE_USAGE_ERROR);
    }
  }
}
