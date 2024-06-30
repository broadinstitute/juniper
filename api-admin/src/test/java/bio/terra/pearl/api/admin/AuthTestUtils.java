package bio.terra.pearl.api.admin;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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

  /** methods we don't validate annotations on -- base Object methods and methods added by Spring */
  private static final List<String> excludedMethodNames =
      List.of(
          "equals",
          "toString",
          "hashCode",
          "indexOf",
          "newInstance",
          "isFrozen",
          "addAdvisor",
          "setCallback",
          "getTargetClass",
          "CGLIB$findMethodProxy",
          "getCallback",
          "getCallbacks",
          "CGLIB$SET_THREAD_CALLBACKS",
          "CGLIB$SET_STATIC_CALLBACKS",
          "setCallbacks",
          "getTargetSource",
          "getProxiedInterfaces",
          "isInterfaceProxied",
          "getAdvisorCount",
          "getAdvisors",
          "isProxyTargetClass",
          "setTargetSource",
          "setExposeProxy",
          "isExposeProxy",
          "setPreFiltered",
          "isPreFiltered",
          "removeAdvisor",
          "replaceAdvisor",
          "removeAdvice",
          "toProxyConfigString",
          "addAdvice");

  /** confirms all public methods of the given class are annotated according to the specs */
  public static void assertAllMethodsAnnotated(
      Object service, Map<String, AuthAnnotationSpec> annotationSpecs) {
    Class<?> serviceClass = service.getClass();
    List<Method> publicMethods =
        Arrays.stream(serviceClass.getDeclaredMethods())
            .filter(
                method ->
                    Modifier.isPublic(method.getModifiers())
                        && !excludedMethodNames.contains(method.getName()))
            .toList();
    for (Method method : publicMethods) {
      AuthAnnotationSpec authSpec = annotationSpecs.get(method.getName());
      if (authSpec == null) {
        throw new IllegalArgumentException(
            "No annotationSpec provided for method: %s.%s"
                .formatted(serviceClass.getSimpleName(), method.getName()));
      }
      validateMethodToSpec(method, authSpec);
    }
  }

  public static void validateMethodToSpec(Method method, AuthAnnotationSpec annotationSpec) {
    if (annotationSpec.permissionAnnotationClass() == null) {
      throw new IllegalArgumentException(
          "annotationSpec for method %s must specify a permission annotation"
              .formatted(method.getName()));
    }
    if (annotationSpec.permissionName() == null) {
      throw new IllegalArgumentException(
          "annotationSpec for method %s must specify a permissionName".formatted(method.getName()));
    }
    Annotation permAnnotation =
        AnnotationUtils.findAnnotation(method, annotationSpec.permissionAnnotationClass());
    assertThat(
        "%s exists on method %s"
            .formatted(
                annotationSpec.permissionAnnotationClass().getSimpleName(), method.getName()),
        permAnnotation,
        notNullValue());
    try {
      String permission = (String) MethodUtils.invokeMethod(permAnnotation, "permission", null);
      assertThat(
          "method %s has permission %s"
              .formatted(method.getName(), annotationSpec.permissionName()),
          permission,
          equalTo(annotationSpec.permissionName()));
    } catch (Exception e) {
      throw new RuntimeException("Could not access permission value of %s annotation on %s method");
    }
    for (Class<? extends Annotation> otherAnnotation : annotationSpec.otherAnnotations()) {
      assertThat(
          "annotation %s exists on method %s"
              .formatted(otherAnnotation.getSimpleName(), method.getName()),
          AnnotationUtils.findAnnotation(method, otherAnnotation),
          notNullValue());
    }
  }
}
