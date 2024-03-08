package bio.terra.pearl.api.admin.service.study;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import bio.terra.pearl.api.admin.BaseSpringBootTest;
import bio.terra.pearl.api.admin.models.dto.StudyCreationDto;
import bio.terra.pearl.core.factory.EnvironmentFactory;
import bio.terra.pearl.core.factory.admin.AdminUserFactory;
import bio.terra.pearl.core.factory.portal.PortalEnvironmentFactory;
import bio.terra.pearl.core.factory.portal.PortalFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import bio.terra.pearl.core.service.notification.TriggerService;
import bio.terra.pearl.core.service.study.PortalStudyService;
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
  @Autowired private PortalEnvironmentFactory portalEnvironmentFactory;
  @Autowired private AdminUserFactory adminUserFactory;
  @Autowired private StudyEnvironmentService studyEnvironmentService;
  @Autowired private StudyService studyService;
  @Autowired private EnvironmentFactory environmentFactory;
  @Autowired private PortalStudyService portalStudyService;
  @Autowired private TriggerService triggerService;

  @Test
  @Transactional
  public void testStudyCreation(TestInfo testInfo) {
    AdminUser operator = adminUserFactory.buildPersisted(getTestName(testInfo), true);
    Portal portal = portalFactory.buildPersisted(getTestName(testInfo));
    for (EnvironmentName envName : EnvironmentName.values()) {
      environmentFactory.buildPersisted(getTestName(testInfo), envName);
    }
    String newStudyShortcode = "newStudy" + RandomStringUtils.randomAlphabetic(5);
    StudyCreationDto studyDto = new StudyCreationDto(newStudyShortcode, "the new study");
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
    StudyCreationDto studyDto = new StudyCreationDto(newStudyShortcode, "the new study");
    Assertions.assertThrows(
        PermissionDeniedException.class,
        () -> {
          studyExtService.create(portal.getShortcode(), studyDto, operator);
        });
  }

  @Test
  @Transactional
  public void testStudyDeletion(TestInfo testInfo) {
    AdminUser operator = adminUserFactory.buildPersisted(getTestName(testInfo), true);
    Portal portal = portalFactory.buildPersisted(getTestName(testInfo));
    for (EnvironmentName envName : EnvironmentName.values()) {
      environmentFactory.buildPersisted(getTestName(testInfo), envName);
    }
    String newStudyShortcode = "newStudy" + RandomStringUtils.randomAlphabetic(5);
    StudyCreationDto studyDto = new StudyCreationDto(newStudyShortcode, "the new study");
    studyExtService.create(portal.getShortcode(), studyDto, operator);

    // confirm study was deleted
    studyExtService.delete(portal.getShortcode(), newStudyShortcode, operator);
    assertThat(studyService.findByShortcode(newStudyShortcode).isEmpty(), equalTo(true));
    // confirm that the corresponding portalService was also deleted
    assertThat(portalStudyService.findByPortalId(portal.getId()), empty());
  }

  @Test
  @Transactional
  public void testStudyDeletionNeedsSuperUser(TestInfo testInfo) {
    AdminUser operator = adminUserFactory.buildPersisted(getTestName(testInfo), true);
    Portal portal = portalFactory.buildPersisted(getTestName(testInfo));
    for (EnvironmentName envName : EnvironmentName.values()) {
      environmentFactory.buildPersisted(getTestName(testInfo), envName);
    }
    String newStudyShortcode = "newStudy" + RandomStringUtils.randomAlphabetic(5);
    StudyCreationDto studyDto = new StudyCreationDto(newStudyShortcode, "the new study");
    studyExtService.create(portal.getShortcode(), studyDto, operator);
    AdminUser deleteOperator = adminUserFactory.buildPersisted(getTestName(testInfo), false);
    // confirm study was not deleted
    Assertions.assertThrows(
        PermissionDeniedException.class,
        () -> {
          studyExtService.delete(portal.getShortcode(), newStudyShortcode, deleteOperator);
        });

    assertThat(studyService.findByShortcode(newStudyShortcode).isEmpty(), equalTo(false));
    // confirm that the corresponding portalService also deleted
    assertThat(portalStudyService.findByPortalId(portal.getId()), not(empty()));
  }

  @Test
  @Transactional
  void testCreateWithTemplate(TestInfo info) {
    Portal portal = portalFactory.buildPersisted(getTestName(info));
    for (EnvironmentName envName : EnvironmentName.values()) {
      environmentFactory.buildPersisted(getTestName(info), envName);
      portalEnvironmentFactory.buildPersisted(getTestName(info), envName, portal.getId());
    }

    AdminUser operator = adminUserFactory.buildPersisted(getTestName(info), true);
    Study study =
        studyExtService.create(
            portal.getShortcode(),
            StudyCreationDto.builder()
                .shortcode("testshortcode")
                .name("Test Study")
                .template(StudyCreationDto.StudyTemplate.BASIC)
                .build(),
            operator);

    Assertions.assertEquals("Test Study", study.getName());
    Assertions.assertEquals("testshortcode", study.getShortcode());

    StudyEnvironment sandboxEnv =
        study.getStudyEnvironments().stream()
            .filter(env -> env.getEnvironmentName().equals(EnvironmentName.sandbox))
            .findFirst()
            .orElseThrow();

    Assertions.assertEquals(6, triggerService.findByStudyEnvironmentId(sandboxEnv.getId()).size());
  }
}
