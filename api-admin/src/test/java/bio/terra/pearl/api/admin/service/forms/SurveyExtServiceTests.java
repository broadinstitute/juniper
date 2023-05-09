package bio.terra.pearl.api.admin.service;

import bio.terra.pearl.api.admin.service.forms.SurveyExtService;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SurveyExtServiceTests {
  private SurveyExtService emptyService = new SurveyExtService(null, null, null);

  @Test
  public void createNewVersionRequiresSuperuser() {
    AdminUser user = AdminUser.builder().superuser(false).build();
    Assertions.assertThrows(
        PermissionDeniedException.class, () -> emptyService.createNewVersion("foo", null, user));
  }
}
