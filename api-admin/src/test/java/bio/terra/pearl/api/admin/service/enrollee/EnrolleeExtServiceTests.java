package bio.terra.pearl.api.admin.service.enrollee;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import bio.terra.pearl.api.admin.BaseSpringBootTest;
import bio.terra.pearl.core.factory.StudyEnvironmentBundle;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.admin.PortalAdminUserFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeBundle;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.service.exception.NotFoundException;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class EnrolleeExtServiceTests extends BaseSpringBootTest {
  @Autowired private StudyEnvironmentFactory studyEnvironmentFactory;
  @Autowired private EnrolleeFactory enrolleeFactory;
  @Autowired private EnrolleeExtService enrolleeExtService;
  @Autowired private PortalAdminUserFactory portalAdminUserFactory;

  @Test
  @Transactional
  public void testFindById(TestInfo info) {
    StudyEnvironmentBundle bundle =
        studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);
    AdminUser operator =
        portalAdminUserFactory
            .buildPersistedWithPortals(getTestName(info), List.of(bundle.getPortal()))
            .user();
    EnrolleeBundle enrollee1 =
        enrolleeFactory.buildWithPortalUser(
            getTestName(info), bundle.getPortalEnv(), bundle.getStudyEnv());

    String portalShortcode = bundle.getPortal().getShortcode();
    String studyShortcode = bundle.getStudy().getShortcode();
    EnvironmentName envName = bundle.getStudyEnv().getEnvironmentName();

    Enrollee loadedEnrollee =
        enrolleeExtService.findWithAdminLoad(
            operator,
            portalShortcode,
            studyShortcode,
            envName,
            enrollee1.enrollee().getShortcode());
    assertThat(loadedEnrollee.getId(), equalTo(enrollee1.enrollee().getId()));

    loadedEnrollee =
        enrolleeExtService.findWithAdminLoad(
            operator,
            portalShortcode,
            studyShortcode,
            envName,
            enrollee1.enrollee().getId().toString());
    assertThat(loadedEnrollee.getId(), equalTo(enrollee1.enrollee().getId()));

    Assertions.assertThrows(
        NotFoundException.class,
        () -> {
          enrolleeExtService.findWithAdminLoad(
              operator, portalShortcode, studyShortcode, envName, UUID.randomUUID().toString());
        });

    Assertions.assertThrows(
        NotFoundException.class,
        () -> {
          enrolleeExtService.findWithAdminLoad(
              operator, portalShortcode, studyShortcode, envName, "BADCODE");
        });
  }
}
