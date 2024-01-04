package bio.terra.pearl.api.admin.service.notifications;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import bio.terra.pearl.api.admin.BaseSpringBootTest;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.notification.NotificationConfigFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.notification.NotificationDeliveryType;
import bio.terra.pearl.core.model.notification.NotificationEventType;
import bio.terra.pearl.core.model.notification.TriggerType;
import bio.terra.pearl.core.model.notification.TriggeredAction;
import bio.terra.pearl.core.service.notification.TriggeredActionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class TriggeredActionExtServiceTests extends BaseSpringBootTest {
  @Autowired StudyEnvironmentFactory studyEnvironmentFactory;
  @Autowired NotificationConfigFactory notificationConfigFactory;
  @Autowired TriggeredActionExtService triggeredActionExtService;
  @Autowired TriggeredActionService triggeredActionService;

  @Test
  @Transactional
  public void testNotificationConfigReplace(TestInfo testInfo) {
    StudyEnvironmentFactory.StudyEnvironmentBundle bundle =
        studyEnvironmentFactory.buildBundle(getTestName(testInfo), EnvironmentName.sandbox);
    TriggeredAction oldConfig =
        notificationConfigFactory.buildPersisted(
            TriggeredAction.builder()
                .triggerType(TriggerType.EVENT)
                .eventType(NotificationEventType.STUDY_CONSENT)
                .deliveryType(NotificationDeliveryType.EMAIL),
            bundle.getStudyEnv().getId(),
            bundle.getPortalEnv().getId());
    AdminUser user = AdminUser.builder().superuser(true).build();
    TriggeredAction update =
        TriggeredAction.builder()
            .triggerType(TriggerType.EVENT)
            .eventType(NotificationEventType.STUDY_ENROLLMENT)
            .deliveryType(NotificationDeliveryType.EMAIL)
            .build();

    TriggeredAction savedConfig =
        triggeredActionExtService.replace(
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

    TriggeredAction updatedOldConfig = triggeredActionService.find(oldConfig.getId()).orElseThrow();
    assertThat(updatedOldConfig.isActive(), equalTo(false));
  }
}
