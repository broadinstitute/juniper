package bio.terra.pearl.api.admin.service.auth;

import bio.terra.pearl.api.admin.service.auth.context.PortalAuthContext;
import java.lang.annotation.Annotation;
import org.apache.commons.lang3.NotImplementedException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;

/** base class for annotations enforcing permissions around methods. */
public abstract class BaseEnforcePermissionAspect<
    T extends PortalAuthContext, A extends Annotation> {
  private static final String ANNOTATION_NAME = "EnforcePermission";
  private static final String AUTH_CONTEXT_USAGE_ERROR =
      "%s annotation must be used on a method whose first argument is a %s";

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
    return extractAuthContext(joinPoint, getAuthContextClass(), ANNOTATION_NAME);
  }

  protected static <Z> Z extractAuthContext(
      JoinPoint joinPoint, Class<Z> clazz, String annotationName) {
    Object[] methodArgs = joinPoint.getArgs();
    if (methodArgs.length == 0) {
      throw new NotImplementedException(
          AUTH_CONTEXT_USAGE_ERROR.formatted(annotationName, clazz.getSimpleName()));
    }
    Object authContext = methodArgs[0];
    if (authContext == null) {
      throw new NotImplementedException("null auth context passed");
    }
    if (clazz.isInstance(authContext)) {
      return clazz.cast(authContext);
    } else {
      throw new NotImplementedException(
          AUTH_CONTEXT_USAGE_ERROR.formatted(annotationName, clazz.getSimpleName()));
    }
  }

  protected abstract Class<T> getAuthContextClass();

  protected abstract Class<A> getAnnotationClass();
}
