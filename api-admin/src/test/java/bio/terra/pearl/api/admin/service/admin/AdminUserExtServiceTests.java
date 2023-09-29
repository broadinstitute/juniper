package bio.terra.pearl.api.admin.service.admin;

import bio.terra.pearl.api.admin.BaseSpringBootTest;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class AdminUserExtServiceTests extends BaseSpringBootTest {
  @Autowired private AdminUserExtService adminUserExtService;

  @Test
  @Transactional
  public void testGetAllRequiresSuperuser() {
    Assertions.assertThrows(
        PermissionDeniedException.class,
        () -> adminUserExtService.getAll(AdminUser.builder().superuser(false).build()));
  }

  @Test
  @Transactional
  public void testGetAllSucceedsWithSuperuser() {
    var result = adminUserExtService.getAll(AdminUser.builder().superuser(true).build());
    Assertions.assertNotNull(result);
  }

  @Test
  @Transactional
  public void testCreateSuperuserFailsForRegularUser() {
    var userToCreate = new AdminUserExtService.NewAdminUser("foo", true, null);
    Assertions.assertThrows(
        PermissionDeniedException.class,
        () ->
            adminUserExtService.create(userToCreate, AdminUser.builder().superuser(false).build()));
  }
}
