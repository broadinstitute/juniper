package bio.terra.pearl.api.admin.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import bio.terra.pearl.api.admin.BaseSpringBootTest;
import bio.terra.pearl.api.admin.service.study.StudyExtService;
import bio.terra.pearl.core.factory.admin.AdminUserFactory;
import bio.terra.pearl.core.factory.portal.PortalFactory;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.study.StudyService;
import java.util.List;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class StudyExtServiceTests extends BaseSpringBootTest {
  @Autowired private StudyExtService studyExtService;
  @Autowired private PortalFactory portalFactory;
  @Autowired private AdminUserFactory adminUserFactory;
  @Autowired private StudyEnvironmentService studyEnvironmentService;
  @Autowired private StudyService studyService;

  @Test
  @Transactional
  public void testStudyCreation(TestInfo testInfo) {
    AdminUser operator = adminUserFactory.buildPersisted(getTestName(testInfo), true);
    Portal portal = portalFactory.buildPersisted(getTestName(testInfo));
    String newStudyShortcode = "newStudy" + RandomStringUtils.randomAlphabetic(5);
    StudyExtService.StudyCreationDto studyDto =
        new StudyExtService.StudyCreationDto(newStudyShortcode, "the new study");
    studyExtService.create(portal.getShortcode(), studyDto, operator);

    // confirm study and environments were created
    Study study = studyService.findByShortcode(newStudyShortcode).get();
    assertThat(study.getName(), equalTo(studyDto.getName()));
    List<StudyEnvironment> newEnvs = studyEnvironmentService.findByStudy(study.getId());
    assertThat(newEnvs.size(), equalTo(3));
  }

  @Test
  @Transactional
  public void testStudyCreationRequiresSuperuser(TestInfo testInfo) {
    AdminUser operator = adminUserFactory.buildPersisted(getTestName(testInfo), false);
    Portal portal = portalFactory.buildPersisted(getTestName(testInfo));
    String newStudyShortcode = "newStudy" + RandomStringUtils.randomAlphabetic(5);
    StudyExtService.StudyCreationDto studyDto =
        new StudyExtService.StudyCreationDto(newStudyShortcode, "the new study");
    Assertions.assertThrows(
        PermissionDeniedException.class,
        () -> {
          studyExtService.create(portal.getShortcode(), studyDto, operator);
        });
  }
}
