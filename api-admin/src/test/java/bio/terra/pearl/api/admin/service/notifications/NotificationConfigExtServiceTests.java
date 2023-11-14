package bio.terra.pearl.api.admin.service.notifications;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import bio.terra.pearl.api.admin.BaseSpringBootTest;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.notification.NotificationConfigFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.notification.NotificationConfig;
import bio.terra.pearl.core.model.notification.NotificationDeliveryType;
import bio.terra.pearl.core.model.notification.NotificationEventType;
import bio.terra.pearl.core.model.notification.NotificationType;
import bio.terra.pearl.core.service.notification.NotificationConfigService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class NotificationConfigExtServiceTests extends BaseSpringBootTest {
  @Autowired StudyEnvironmentFactory studyEnvironmentFactory;
  @Autowired NotificationConfigFactory notificationConfigFactory;
  @Autowired NotificationConfigExtService notificationConfigExtService;
  @Autowired NotificationConfigService notificationConfigService;

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
}
