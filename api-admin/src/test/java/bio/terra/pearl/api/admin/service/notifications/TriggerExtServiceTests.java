package bio.terra.pearl.api.admin.service.notifications;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import bio.terra.pearl.api.admin.BaseSpringBootTest;
import bio.terra.pearl.api.admin.service.auth.context.PortalStudyEnvAuthContext;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.notification.NotificationFactory;
import bio.terra.pearl.core.factory.notification.TriggerFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.notification.NotificationDeliveryType;
import bio.terra.pearl.core.model.notification.Trigger;
import bio.terra.pearl.core.model.notification.TriggerEventType;
import bio.terra.pearl.core.model.notification.TriggerType;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.notification.TriggerService;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class TriggerExtServiceTests extends BaseSpringBootTest {
  @Autowired StudyEnvironmentFactory studyEnvironmentFactory;
  @Autowired TriggerFactory triggerFactory;
  @Autowired TriggerExtService triggerExtService;
  @Autowired TriggerService triggerService;
  @Autowired NotificationFactory notificationFactory;
  @Autowired EnrolleeFactory enrolleeFactory;

  @Test
  @Transactional
  public void testNotificationConfigReplace(TestInfo testInfo) {
    StudyEnvironmentFactory.StudyEnvironmentBundle bundle =
        studyEnvironmentFactory.buildBundle(getTestName(testInfo), EnvironmentName.sandbox);
    Trigger oldConfig =
        triggerFactory.buildPersisted(
            Trigger.builder()
                .triggerType(TriggerType.EVENT)
                .eventType(TriggerEventType.STUDY_CONSENT)
                .deliveryType(NotificationDeliveryType.EMAIL),
            bundle.getStudyEnv().getId(),
            bundle.getPortalEnv().getId());
    AdminUser user = AdminUser.builder().superuser(true).build();
    Trigger update =
        Trigger.builder()
            .triggerType(TriggerType.EVENT)
            .eventType(TriggerEventType.STUDY_ENROLLMENT)
            .deliveryType(NotificationDeliveryType.EMAIL)
            .build();

    Trigger savedConfig =
        triggerExtService.replace(
            PortalStudyEnvAuthContext.of(
                user,
                bundle.getPortal().getShortcode(),
                bundle.getStudy().getShortcode(),
                bundle.getStudyEnv().getEnvironmentName()),
            oldConfig.getId(),
            update);
    assertThat(savedConfig.isActive(), equalTo(true));
    assertThat(savedConfig.getStudyEnvironmentId(), equalTo(bundle.getStudyEnv().getId()));
    assertThat(savedConfig.getPortalEnvironmentId(), equalTo(bundle.getPortalEnv().getId()));
    assertThat(savedConfig.getEventType(), equalTo(TriggerEventType.STUDY_ENROLLMENT));

    Trigger updatedOldConfig = triggerService.find(oldConfig.getId()).orElseThrow();
    assertThat(updatedOldConfig.isActive(), equalTo(false));
  }

  @Test
  @Transactional
  public void testDeleteNotificationConfigNotFound(TestInfo testInfo) {
    StudyEnvironmentFactory.StudyEnvironmentBundle bundle =
        studyEnvironmentFactory.buildBundle(getTestName(testInfo), EnvironmentName.sandbox);

    AdminUser user = AdminUser.builder().superuser(true).build();

    Assertions.assertThrows(
        NotFoundException.class,
        () ->
            triggerExtService.delete(
                PortalStudyEnvAuthContext.of(
                    user,
                    bundle.getPortal().getShortcode(),
                    bundle.getStudy().getShortcode(),
                    bundle.getStudyEnv().getEnvironmentName()),
                UUID.randomUUID()));
  }

  @Test
  @Transactional
  public void testDeleteNotificationConfigMustBeAuthenticated(TestInfo testInfo) {
    StudyEnvironmentFactory.StudyEnvironmentBundle bundle =
        studyEnvironmentFactory.buildBundle(getTestName(testInfo), EnvironmentName.sandbox);

    Trigger config =
        triggerFactory.buildPersisted(
            Trigger.builder()
                .triggerType(TriggerType.EVENT)
                .eventType(TriggerEventType.STUDY_CONSENT)
                .deliveryType(NotificationDeliveryType.EMAIL),
            bundle.getStudyEnv().getId(),
            bundle.getPortalEnv().getId());

    // not a superuser and not a part of the portals
    AdminUser user = AdminUser.builder().superuser(false).build();

    Assertions.assertTrue(triggerService.find(config.getId()).isPresent());

    // should throw not found in this case
    Assertions.assertThrows(
        NotFoundException.class,
        () ->
            triggerExtService.delete(
                PortalStudyEnvAuthContext.of(
                    user,
                    bundle.getPortal().getShortcode(),
                    bundle.getStudy().getShortcode(),
                    bundle.getStudyEnv().getEnvironmentName()),
                config.getId()));
  }

  @Test
  @Transactional
  public void testDeleteNotificationConfig(TestInfo testInfo) {
    StudyEnvironmentFactory.StudyEnvironmentBundle bundle =
        studyEnvironmentFactory.buildBundle(getTestName(testInfo), EnvironmentName.sandbox);

    Trigger config =
        triggerFactory.buildPersisted(
            Trigger.builder()
                .triggerType(TriggerType.EVENT)
                .eventType(TriggerEventType.STUDY_CONSENT)
                .deliveryType(NotificationDeliveryType.EMAIL),
            bundle.getStudyEnv().getId(),
            bundle.getPortalEnv().getId());

    AdminUser user = AdminUser.builder().superuser(true).build();

    Assertions.assertTrue(triggerService.find(config.getId()).isPresent());

    triggerExtService.delete(
        PortalStudyEnvAuthContext.of(
            user,
            bundle.getPortal().getShortcode(),
            bundle.getStudy().getShortcode(),
            bundle.getStudyEnv().getEnvironmentName()),
        config.getId());

    // should still exist, but active should be false
    Optional<Trigger> configOpt = triggerService.find(config.getId());
    Assertions.assertTrue(configOpt.isPresent());
    Assertions.assertFalse(configOpt.get().isActive());
  }
}
