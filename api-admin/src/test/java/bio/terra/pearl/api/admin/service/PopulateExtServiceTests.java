package bio.terra.pearl.api.admin.service;

import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PopulateExtServiceTests {
  private PopulateExtService emptyService =
      new PopulateExtService(null, null, null, null, null, null, null, null, portalExportService);

  @Test
  public void baseSeedRequiresAuth() {
    AdminUser user = new AdminUser();
    Assertions.assertThrows(
        PermissionDeniedException.class, () -> emptyService.populateBaseSeed(user));
  }

  @Test
  public void portalRequiresAuth() {
    AdminUser user = new AdminUser();
    Assertions.assertThrows(
        PermissionDeniedException.class, () -> emptyService.populatePortal("dfafd", user, false));
  }

  @Test
  public void surveyRequiresAuth() {
    AdminUser user = new AdminUser();
    Assertions.assertThrows(
        PermissionDeniedException.class,
        () -> emptyService.populateSurvey("dsafsd", "filepath", user, false));
  }

  @Test
  public void enrolleeRequiresAuth() {
    AdminUser user = new AdminUser();
    Assertions.assertThrows(
        PermissionDeniedException.class,
        () ->
            emptyService.populateEnrollee(
                "ffo", EnvironmentName.live, "dfa", "dfadf", user, false));
  }

  @Test
  public void siteContentRequiresAuth() {
    AdminUser user = new AdminUser();
    Assertions.assertThrows(
        PermissionDeniedException.class,
        () -> emptyService.populateSiteContent("ffo", "dfa", user, false));
  }

  @Test
  public void bulkEnrolleeRequiresAuth() {
    AdminUser user = new AdminUser();
    Assertions.assertThrows(
        PermissionDeniedException.class,
        () -> emptyService.bulkPopulateEnrollees("ffo", EnvironmentName.live, "dfa", 100, user));
  }
}
