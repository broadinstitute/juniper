package bio.terra.pearl.api.admin;

import bio.terra.pearl.api.admin.service.auth.EnforcePortalPermission;
import bio.terra.pearl.api.admin.service.auth.SandboxOnly;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AuthTestUtilsTest {
  @Test
  public void testChecksAnnotatedMethod() {
    AuthTestUtils.assertAllMethodsAnnotated(
        new MethodAnnotatedClass(),
        Map.of("doSomethingSecure", AuthAnnotationSpec.withPortalPerm("BASE")));

    // fails if the wrong permission is specced
    Assertions.assertThrows(
        AssertionError.class,
        () -> {
          AuthTestUtils.assertAllMethodsAnnotated(
              new MethodAnnotatedClass(),
              Map.of("doSomethingSecure", AuthAnnotationSpec.withPortalPerm("wrongPerm")));
        });

    // fails if the spec also expects a SandboxOnly annotation
    Assertions.assertThrows(
        AssertionError.class,
        () -> {
          AuthTestUtils.assertAllMethodsAnnotated(
              new MethodAnnotatedClass(),
              Map.of(
                  "doSomethingSecure",
                  AuthAnnotationSpec.withPortalPerm("wrongPerm", List.of(SandboxOnly.class))));
        });
  }

  @Test
  public void testFailsUnspecifiedMethod() {
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> {
          AuthTestUtils.assertAllMethodsAnnotated(new MethodAnnotatedClass(), Map.of());
        });
  }

  @Test
  public void testFailsMethodNotAnnotated() {
    Assertions.assertThrows(
        AssertionError.class,
        () -> {
          AuthTestUtils.assertAllMethodsAnnotated(
              new MethodNotAnnotatedClass(),
              Map.of("doSomethingSecure", AuthAnnotationSpec.withPortalPerm("BASE")));
        });
  }

  @Test
  public void testChecksSandboxAnnotated() {
    AuthTestUtils.assertAllMethodsAnnotated(
        new SandboxMethodAnnotatedClass(),
        Map.of(
            "doSomethingSecure",
            AuthAnnotationSpec.withPortalPerm("BASE", List.of(SandboxOnly.class))));
  }

  public static class MethodAnnotatedClass {
    @EnforcePortalPermission(permission = "BASE")
    public void doSomethingSecure() {}
  }

  public static class MethodNotAnnotatedClass {
    public void doSomethingSecure() {}
  }

  public static class SandboxMethodAnnotatedClass {
    @SandboxOnly
    @EnforcePortalPermission(permission = "BASE")
    public void doSomethingSecure() {}
  }
}
