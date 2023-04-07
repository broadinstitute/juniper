package bio.terra.pearl.api.admin.service;

import bio.terra.pearl.api.admin.MockAuthServiceAlwaysRejects;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MailingServiceExtServiceTests {
  @Test
  public void mailingListRequiresAuth() {
    var listExtService = new MailingListExtService(new MockAuthServiceAlwaysRejects(), null, null);
    // testing that this exception is thrown even when everything else is null is a good check that
    // no work is done prior to auth
    Assertions.assertThrows(
        PermissionDeniedException.class,
        () -> listExtService.getAll("ourhealth", EnvironmentName.live, new AdminUser()));
  }
}
