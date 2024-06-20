package bio.terra.pearl.api.admin;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.beanutils.MethodUtils;
import org.springframework.core.annotation.AnnotationUtils;

public class AuthTestUtils {

  public static void assertHasAnnotation(
      Object service, String methodName, Class<? extends Annotation> annotationClass) {
    List<Method> targetMethods =
        Arrays.stream(service.getClass().getDeclaredMethods())
            .filter(method -> method.getName().equals(methodName))
            .toList();
    assertThat(targetMethods.size(), greaterThan(0));
    targetMethods
        .stream() // we need to check all methods with the same name in case they are overloaded
        .forEach(
            targetMethod ->
                assertThat(
                    AnnotationUtils.findAnnotation(targetMethod, annotationClass), notNullValue()));
  }

  public static void assertHasPermissionEnforced(
      Object service,
      String methodName,
      Class<? extends Annotation> annotationClass,
      String permissionName) {
    List<Method> targetMethods =
        Arrays.stream(service.getClass().getDeclaredMethods())
            .filter(method -> method.getName().equals(methodName))
            .toList();
    assertThat(targetMethods.size(), greaterThan(0));
    targetMethods
        .stream() // we need to check all methods with the same name in case they are overloaded
        .forEach(
            targetMethod -> {
              Annotation annotation = AnnotationUtils.findAnnotation(targetMethod, annotationClass);
              try {
                String permission =
                    (String) MethodUtils.invokeMethod(annotation, "permission", null);
                assertThat(permission, equalTo(permissionName));
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            });
  }
}
