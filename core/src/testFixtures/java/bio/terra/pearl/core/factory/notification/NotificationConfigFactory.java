package bio.terra.pearl.core.factory.notification;

import bio.terra.pearl.core.factory.portal.PortalEnvironmentFactory;
import bio.terra.pearl.core.model.notification.NotificationConfig;
import bio.terra.pearl.core.model.notification.NotificationDeliveryType;
import bio.terra.pearl.core.model.notification.NotificationType;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.service.notification.NotificationConfigService;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationConfigFactory {
    @Autowired
    private NotificationConfigService notificationConfigService;
    @Autowired
    private PortalEnvironmentFactory portalEnvironmentFactory;

    public NotificationConfig buildPersisted(NotificationConfig.NotificationConfigBuilder builder, UUID studyEnvId,UUID portalEnvId) {
        NotificationConfig config = builder.studyEnvironmentId(studyEnvId)
                .portalEnvironmentId(portalEnvId)
                .build();
        return notificationConfigService.create(config);
    }

    public NotificationConfig buildPersisted(String testName) {
        PortalEnvironment portalEnvironment = portalEnvironmentFactory.buildPersisted(testName);
        var builder = NotificationConfig.builder()
            .portalEnvironmentId(portalEnvironment.getId())
            .notificationType(NotificationType.EVENT)
            .deliveryType(NotificationDeliveryType.EMAIL);
        return buildPersisted(builder, null, portalEnvironment.getId());
    }
}
