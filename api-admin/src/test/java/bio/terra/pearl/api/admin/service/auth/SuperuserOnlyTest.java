package bio.terra.pearl.api.admin.service.auth;

import bio.terra.pearl.api.admin.BaseSpringBootTest;
import bio.terra.pearl.api.admin.service.auth.context.OperatorAuthContext;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class SuperuserOnlyTest extends BaseSpringBootTest {
  @Autowired private SuperuserOnlyTestBean superuserOnlyTestBean;

  @Test
  public void testSuperUserOnlyAllowsSuperUser() {
    AdminUser superuser = AdminUser.builder().superuser(true).build();
    Assertions.assertDoesNotThrow(
        () -> superuserOnlyTestBean.superuserOnlyMethod(OperatorAuthContext.of(superuser)));
  }

  @Test
  public void testSuperUserOnlyDisallowsOtherUser() {
    AdminUser superuser = AdminUser.builder().superuser(false).build();
    Assertions.assertThrows(
        PermissionDeniedException.class,
        () -> superuserOnlyTestBean.superuserOnlyMethod(OperatorAuthContext.of(superuser)));
  }

  @Test
  public void testSuperUserOnlyErrorsOnNull() {
    Assertions.assertThrows(
        NotImplementedException.class, () -> superuserOnlyTestBean.superuserOnlyMethod(null));
  }

  @Test
  public void testSuperuserOnlyNoAuthContextErrors() {
    Assertions.assertThrows(
        NotImplementedException.class,
        () -> superuserOnlyTestBean.superuserOnlyMethodNoAuthContext("foo"));
  }
}
