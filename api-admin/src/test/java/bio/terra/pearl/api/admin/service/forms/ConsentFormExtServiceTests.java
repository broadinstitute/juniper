package bio.terra.pearl.api.admin.service.forms;

import bio.terra.pearl.api.admin.BaseSpringBootTest;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.admin.AdminUserFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.admin.PortalAdminUser;
import bio.terra.pearl.core.model.consent.StudyEnvironmentConsent;
import bio.terra.pearl.core.service.admin.PortalAdminUserService;
import bio.terra.pearl.core.service.exception.NotFoundException;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class ConsentFormExtServiceTests extends BaseSpringBootTest {
  @Autowired private AdminUserFactory adminUserFactory;
  @Autowired private StudyEnvironmentFactory studyEnvironmentFactory;
  @Autowired private ConsentFormExtService consentFormExtService;
  @Autowired private PortalAdminUserService portalAdminUserService;

  @Test
  @Transactional
  public void testUpdateConfiguredConsentAuth(TestInfo testInfo) {
    AdminUser user = adminUserFactory.buildPersisted(getTestName(testInfo), false);
    var envBundle = studyEnvironmentFactory.buildBundle(getTestName(testInfo), EnvironmentName.irb);

    // auths to portal
    Assertions.assertThrows(
        NotFoundException.class,
        () -> {
          consentFormExtService.updateConfiguredConsent(
              envBundle.getPortal().getShortcode(),
              envBundle.getPortalEnv().getEnvironmentName(),
              envBundle.getStudy().getShortcode(),
              StudyEnvironmentConsent.builder()
                  .consentFormId(UUID.randomUUID())
                  .studyEnvironmentId(envBundle.getStudyEnv().getId())
                  .build(),
              user);
        });

    portalAdminUserService.create(
        PortalAdminUser.builder()
            .portalId(envBundle.getPortal().getId())
            .adminUserId(user.getId())
            .build());

    // can't directly update IRB environment
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> {
          consentFormExtService.updateConfiguredConsent(
              envBundle.getPortal().getShortcode(),
              envBundle.getPortalEnv().getEnvironmentName(),
              envBundle.getStudy().getShortcode(),
              StudyEnvironmentConsent.builder()
                  .consentFormId(UUID.randomUUID())
                  .studyEnvironmentId(envBundle.getStudyEnv().getId())
                  .build(),
              user);
        });
  }

  @Test
  @Transactional
  public void testCreateConfiguredConsentAuth(TestInfo testInfo) {
    AdminUser user = adminUserFactory.buildPersisted(getTestName(testInfo), false);
    var envBundle =
        studyEnvironmentFactory.buildBundle(getTestName(testInfo), EnvironmentName.live);

    // auths to portal
    Assertions.assertThrows(
        NotFoundException.class,
        () -> {
          consentFormExtService.createConfiguredConsent(
              envBundle.getPortal().getShortcode(),
              envBundle.getPortalEnv().getEnvironmentName(),
              envBundle.getStudy().getShortcode(),
              StudyEnvironmentConsent.builder()
                  .consentFormId(UUID.randomUUID())
                  .studyEnvironmentId(envBundle.getStudyEnv().getId())
                  .build(),
              user);
        });

    portalAdminUserService.create(
        PortalAdminUser.builder()
            .portalId(envBundle.getPortal().getId())
            .adminUserId(user.getId())
            .build());

    // can't directly update Live environment
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> {
          consentFormExtService.createConfiguredConsent(
              envBundle.getPortal().getShortcode(),
              envBundle.getPortalEnv().getEnvironmentName(),
              envBundle.getStudy().getShortcode(),
              StudyEnvironmentConsent.builder()
                  .consentFormId(UUID.randomUUID())
                  .studyEnvironmentId(envBundle.getStudyEnv().getId())
                  .build(),
              user);
        });
  }
}
