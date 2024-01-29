package bio.terra.pearl.api.admin.service.study;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import bio.terra.pearl.api.admin.BaseSpringBootTest;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.admin.AdminUserFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.study.StudyEnvironmentConfig;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.study.StudyEnvironmentConfigService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class StudyEnvironmentExtServiceTests extends BaseSpringBootTest {
  @Autowired private StudyEnvironmentExtService studyEnvironmentExtService;
  @Autowired private StudyEnvironmentFactory studyEnvironmentFactory;
  @Autowired private AdminUserFactory adminUserFactory;
  @Autowired private StudyEnvironmentConfigService studyEnvironmentConfigService;

  @Test
  @Transactional
  public void updateConfigAuthsToStudy(TestInfo info) {
    var studyEnvBundle =
        studyEnvironmentFactory.buildBundle("updateConfigAuthsToStudy", EnvironmentName.irb);
    AdminUser operator = adminUserFactory.buildPersisted(getTestName(info), false);
    Assertions.assertThrows(
        NotFoundException.class,
        () -> {
          studyEnvironmentExtService.updateConfig(
              operator,
              studyEnvBundle.getPortal().getShortcode(),
              studyEnvBundle.getStudy().getShortcode(),
              EnvironmentName.irb,
              new StudyEnvironmentConfig());
        });
  }

  @Test
  @Transactional
  public void updateConfigAllowsSuperuser(TestInfo info) {
    var studyEnvBundle =
        studyEnvironmentFactory.buildBundle("updateConfigAllowsSuperuser", EnvironmentName.irb);
    AdminUser superUser = adminUserFactory.buildPersisted(getTestName(info), true);
    studyEnvironmentExtService.updateConfig(
        superUser,
        studyEnvBundle.getPortal().getShortcode(),
        studyEnvBundle.getStudy().getShortcode(),
        EnvironmentName.irb,
        StudyEnvironmentConfig.builder().password("test456").build());

    var updatedConfig =
        studyEnvironmentConfigService
            .find(studyEnvBundle.getStudyEnv().getStudyEnvironmentConfigId())
            .get();
    assertThat(updatedConfig.getPassword(), equalTo("test456"));
  }
}
