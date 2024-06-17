package bio.terra.pearl.api.admin.service.auth;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import bio.terra.pearl.api.admin.BaseSpringBootTest;
import bio.terra.pearl.api.admin.service.auth.context.PortalEnvAuthContext;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class SandboxOnlyTest extends BaseSpringBootTest {
  @Autowired private SandboxOnlyAnnotationTestBean sandboxOnlyAnnotationTestBean;

  @Test
  public void testSandboxOnlyMethodAllowsSandbox() {
    sandboxOnlyAnnotationTestBean.sandboxOnlyMethod(
        PortalEnvAuthContext.of(new AdminUser(), "foo", EnvironmentName.sandbox));
    assertThat(true, equalTo(true));
  }

  @Test
  public void testSandboxOnlyMethodDisallowsOther() {
    Assertions.assertThrows(
        UnsupportedOperationException.class,
        () ->
            sandboxOnlyAnnotationTestBean.sandboxOnlyMethod(
                PortalEnvAuthContext.of(new AdminUser(), "foo", EnvironmentName.irb)));
  }

  @Test
  public void testSandboxOnlyMethodErrorsOnNull() {
    Assertions.assertThrows(
        NotImplementedException.class, () -> sandboxOnlyAnnotationTestBean.sandboxOnlyMethod(null));
  }

  @Test
  public void testSandboxOnlyNoAuthContextErrors() {
    Assertions.assertThrows(
        NotImplementedException.class,
        () -> sandboxOnlyAnnotationTestBean.sandboxOnlyMethodNoAuthContext("someArg"));
  }
}
