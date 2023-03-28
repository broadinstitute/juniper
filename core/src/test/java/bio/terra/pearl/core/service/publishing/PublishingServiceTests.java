package bio.terra.pearl.core.service.publishing;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.model.notification.EmailTemplate;
import bio.terra.pearl.core.model.notification.NotificationConfig;
import bio.terra.pearl.core.model.notification.NotificationEventType;
import bio.terra.pearl.core.model.notification.NotificationType;
import org.junit.jupiter.api.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class PublishingServiceTests extends BaseSpringBootTest {

    @Test
    public void testIsNotificationConfigMatch() {
        var config = NotificationConfig.builder()
                .notificationType(NotificationType.EVENT)
                .eventType(NotificationEventType.STUDY_CONSENT)
                .emailTemplate(new EmailTemplate()).build();

        var configWithDifferentTemplate  = NotificationConfig.builder()
                .notificationType(NotificationType.EVENT)
                .eventType(NotificationEventType.STUDY_CONSENT)
                .emailTemplate(new EmailTemplate()).build();
        assertThat(PublishingService.isNotificationConfigMatch(config, configWithDifferentTemplate), equalTo(true));
    }

    @Test
    public void testIsNotificationConfigMatchDifferentEvent() {
        var config = NotificationConfig.builder()
                .notificationType(NotificationType.EVENT)
                .eventType(NotificationEventType.STUDY_CONSENT)
                .emailTemplate(new EmailTemplate()).build();

        var configWithDifferentEventType  = NotificationConfig.builder()
                .notificationType(NotificationType.EVENT)
                .eventType(NotificationEventType.STUDY_ENROLLMENT)
                .emailTemplate(new EmailTemplate()).build();
        assertThat(PublishingService.isNotificationConfigMatch(config, configWithDifferentEventType), equalTo(false));
    }
}
