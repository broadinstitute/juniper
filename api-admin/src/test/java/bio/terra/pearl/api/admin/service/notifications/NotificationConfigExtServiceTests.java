package bio.terra.pearl.api.admin.service.notifications;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import bio.terra.pearl.api.admin.BaseSpringBootTest;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.notification.NotificationConfigFactory;
import bio.terra.pearl.core.factory.notification.NotificationFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.notification.NotificationConfig;
import bio.terra.pearl.core.model.notification.NotificationDeliveryType;
import bio.terra.pearl.core.model.notification.NotificationEventType;
import bio.terra.pearl.core.model.notification.NotificationType;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.notification.NotificationConfigService;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class NotificationConfigExtServiceTests extends BaseSpringBootTest {
  @Autowired StudyEnvironmentFactory studyEnvironmentFactory;
  @Autowired NotificationConfigFactory notificationConfigFactory;
  @Autowired NotificationFactory notificationFactory;
  @Autowired NotificationConfigExtService notificationConfigExtService;
  @Autowired NotificationConfigService notificationConfigService;
  @Autowired EnrolleeFactory enrolleeFactory;

  @Test
  @Transactional
  public void testNotificationConfigReplace(TestInfo testInfo) {
    StudyEnvironmentFactory.StudyEnvironmentBundle bundle =
        studyEnvironmentFactory.buildBundle(getTestName(testInfo), EnvironmentName.sandbox);
    NotificationConfig oldConfig =
        notificationConfigFactory.buildPersisted(
            NotificationConfig.builder()
                .notificationType(NotificationType.EVENT)
                .eventType(NotificationEventType.STUDY_CONSENT)
                .deliveryType(NotificationDeliveryType.EMAIL),
            bundle.getStudyEnv().getId(),
            bundle.getPortalEnv().getId());
    AdminUser user = AdminUser.builder().superuser(true).build();
    NotificationConfig update =
        NotificationConfig.builder()
            .notificationType(NotificationType.EVENT)
            .eventType(NotificationEventType.STUDY_ENROLLMENT)
            .deliveryType(NotificationDeliveryType.EMAIL)
            .build();

    NotificationConfig savedConfig =
        notificationConfigExtService.replace(
            bundle.getPortal().getShortcode(),
            bundle.getStudy().getShortcode(),
            bundle.getStudyEnv().getEnvironmentName(),
            oldConfig.getId(),
            update,
            user);
    assertThat(savedConfig.isActive(), equalTo(true));
    assertThat(savedConfig.getStudyEnvironmentId(), equalTo(bundle.getStudyEnv().getId()));
    assertThat(savedConfig.getPortalEnvironmentId(), equalTo(bundle.getPortalEnv().getId()));
    assertThat(savedConfig.getEventType(), equalTo(NotificationEventType.STUDY_ENROLLMENT));

    NotificationConfig updatedOldConfig =
        notificationConfigService.find(oldConfig.getId()).orElseThrow();
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
            notificationConfigExtService.delete(
                user,
                bundle.getPortal().getShortcode(),
                bundle.getStudy().getShortcode(),
                bundle.getStudyEnv().getEnvironmentName(),
                UUID.randomUUID()));
  }

  @Test
  @Transactional
  public void testDeleteNotificationConfigMustBeAuthenticated(TestInfo testInfo) {
    StudyEnvironmentFactory.StudyEnvironmentBundle bundle =
        studyEnvironmentFactory.buildBundle(getTestName(testInfo), EnvironmentName.sandbox);

    NotificationConfig config =
        notificationConfigFactory.buildPersisted(
            NotificationConfig.builder()
                .notificationType(NotificationType.EVENT)
                .eventType(NotificationEventType.STUDY_CONSENT)
                .deliveryType(NotificationDeliveryType.EMAIL),
            bundle.getStudyEnv().getId(),
            bundle.getPortalEnv().getId());

    // not a superuser and not a part of the portals
    AdminUser user = AdminUser.builder().superuser(false).build();

    Assertions.assertTrue(notificationConfigService.find(config.getId()).isPresent());

    // should throw not found in this case
    Assertions.assertThrows(
        NotFoundException.class,
        () ->
            notificationConfigExtService.delete(
                user,
                bundle.getPortal().getShortcode(),
                bundle.getStudy().getShortcode(),
                bundle.getStudyEnv().getEnvironmentName(),
                config.getId()));
  }

  @Test
  @Transactional
  public void testDeleteNotificationConfig(TestInfo testInfo) {
    StudyEnvironmentFactory.StudyEnvironmentBundle bundle =
        studyEnvironmentFactory.buildBundle(getTestName(testInfo), EnvironmentName.sandbox);

    NotificationConfig config =
        notificationConfigFactory.buildPersisted(
            NotificationConfig.builder()
                .notificationType(NotificationType.EVENT)
                .eventType(NotificationEventType.STUDY_CONSENT)
                .deliveryType(NotificationDeliveryType.EMAIL),
            bundle.getStudyEnv().getId(),
            bundle.getPortalEnv().getId());

    AdminUser user = AdminUser.builder().superuser(true).build();

    Assertions.assertTrue(notificationConfigService.find(config.getId()).isPresent());

    notificationConfigExtService.delete(
        user,
        bundle.getPortal().getShortcode(),
        bundle.getStudy().getShortcode(),
        bundle.getStudyEnv().getEnvironmentName(),
        config.getId());

    // should still exist, but active should be false
    Optional<NotificationConfig> configOpt = notificationConfigService.find(config.getId());
    Assertions.assertTrue(configOpt.isPresent());
    Assertions.assertFalse(configOpt.get().isActive());
  }
}
