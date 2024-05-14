package bio.terra.pearl.api.admin.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

import bio.terra.pearl.api.admin.BaseSpringBootTest;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.admin.AdminUserFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.populate.service.EnrolleePopulateType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;

public class PopulateExtServiceTests extends BaseSpringBootTest {
  private PopulateExtService emptyService =
      new PopulateExtService(null, null, null, null, null, null, null, null);

  @Autowired private PopulateExtService populateExtService;
  @Autowired private StudyEnvironmentFactory studyEnvironmentFactory;
  @Autowired private AdminUserFactory adminUserFactory;
  @Autowired private EnrolleeService enrolleeService;

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
        PermissionDeniedException.class,
        () -> emptyService.populatePortal("dfafd", user, false, null));
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

  @Test
  public void populatesNewEnrolleeType(TestInfo info) {
    StudyEnvironmentFactory.StudyEnvironmentBundle bundle =
        studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.live);
    AdminUser user = adminUserFactory.buildPersisted(getTestName(info), true);
    populateExtService.populateEnrollee(
        bundle.getPortal().getShortcode(),
        bundle.getPortalEnv().getEnvironmentName(),
        bundle.getStudy().getShortcode(),
        EnrolleePopulateType.NEW,
        user);
    assertThat(enrolleeService.findByStudyEnvironment(bundle.getStudyEnv().getId()), hasSize(1));
  }
}
