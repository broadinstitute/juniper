package bio.terra.pearl.core.factory.notification;

import bio.terra.pearl.core.factory.portal.PortalEnvironmentFactory;
import bio.terra.pearl.core.model.notification.TriggeredAction;
import bio.terra.pearl.core.model.notification.NotificationDeliveryType;
import bio.terra.pearl.core.model.notification.TriggerType;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.service.notification.TriggeredActionService;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationConfigFactory {
    @Autowired
    private TriggeredActionService triggeredActionService;
    @Autowired
    private PortalEnvironmentFactory portalEnvironmentFactory;

    public TriggeredAction buildPersisted(TriggeredAction.NotificationConfigBuilder builder, UUID studyEnvId, UUID portalEnvId) {
        TriggeredAction config = builder.studyEnvironmentId(studyEnvId)
                .portalEnvironmentId(portalEnvId)
                .build();
        return triggeredActionService.create(config);
    }

    public TriggeredAction buildPersisted(String testName) {
        PortalEnvironment portalEnvironment = portalEnvironmentFactory.buildPersisted(testName);
        var builder = TriggeredAction.builder()
            .portalEnvironmentId(portalEnvironment.getId())
            .triggerType(TriggerType.EVENT)
            .deliveryType(NotificationDeliveryType.EMAIL);
        return buildPersisted(builder, null, portalEnvironment.getId());
    }
}
