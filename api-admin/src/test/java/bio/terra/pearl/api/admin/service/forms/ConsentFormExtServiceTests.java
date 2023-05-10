package bio.terra.pearl.api.admin.service.forms;

import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConsentFormExtServiceTests {
  private ConsentFormExtService emptyService = new ConsentFormExtService(null, null);

  @Test
  public void createNewVersionRequiresSuperuser() {
    AdminUser user = AdminUser.builder().superuser(false).build();
    Assertions.assertThrows(
        PermissionDeniedException.class, () -> emptyService.createNewVersion("foo", null, user));
  }
}
