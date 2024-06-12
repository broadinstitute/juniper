package bio.terra.pearl.api.admin.service.auth;

import java.lang.annotation.Annotation;
import org.apache.commons.lang3.NotImplementedException;
import org.aspectj.lang.ProceedingJoinPoint;

/** base class for annotations enforcing permissions around methods. */
public abstract class BaseEnforcePermissionAspect<
    T extends PortalAuthContext, A extends Annotation> {
  private static final String AUTH_CONTEXT_USAGE_ERROR =
      "EnforcePortalPermission annotation must be used on a method whose first argument is a %s";

  /**
   * you would think we could implement this method in the base class, but because interfaces and
   * annotations don't support inheritance, there's no way for this method to be sure A supports a
   * method called permission().
   */
  protected abstract String getPermissionName(ProceedingJoinPoint joinPoint);

  public abstract Object enforcePermission(ProceedingJoinPoint joinPoint) throws Throwable;

  /**
   * this gets the authContext by assuming it's the first argument to the method. It's probably good
   * to enforce that the AdminUser is always the first arg for consistency's sake. However, we could
   * eventually get fancy and use a parameter annotation to detect it. If something goes awry, this
   * throws NotImplementedException, since it's a programming error.
   */
  protected T extractAuthContext(ProceedingJoinPoint joinPoint) {
    Object[] methodArgs = joinPoint.getArgs();
    if (methodArgs.length == 0) {
      throw new NotImplementedException(
          AUTH_CONTEXT_USAGE_ERROR.formatted(getAuthContextClass().getSimpleName()));
    }
    Object authContext = methodArgs[0];
    if (authContext == null) {
      throw new NotImplementedException("null auth context passed");
    }
    if (getAuthContextClass().isInstance(authContext)) {
      return getAuthContextClass().cast(authContext);
    } else {
      throw new NotImplementedException(
          AUTH_CONTEXT_USAGE_ERROR.formatted(getAuthContextClass().getSimpleName()));
    }
  }

  protected abstract Class<T> getAuthContextClass();

  protected abstract Class<A> getAnnotationClass();
}
