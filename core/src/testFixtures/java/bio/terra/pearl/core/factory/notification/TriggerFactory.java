package bio.terra.pearl.core.factory.notification;

import bio.terra.pearl.core.factory.portal.PortalEnvironmentFactory;
import bio.terra.pearl.core.model.notification.NotificationDeliveryType;
import bio.terra.pearl.core.model.notification.Trigger;
import bio.terra.pearl.core.model.notification.Trigger.TriggerBuilder;
import bio.terra.pearl.core.model.notification.TriggerType;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.service.notification.TriggerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TriggerFactory {
    @Autowired
    private TriggerService triggerService;
    @Autowired
    private PortalEnvironmentFactory portalEnvironmentFactory;

    public Trigger buildPersisted(TriggerBuilder builder, UUID studyEnvId, UUID portalEnvId) {
        Trigger config = builder.studyEnvironmentId(studyEnvId)
                .portalEnvironmentId(portalEnvId)
                .build();
        return triggerService.create(config);
    }

    public Trigger buildPersisted(String testName) {
        PortalEnvironment portalEnvironment = portalEnvironmentFactory.buildPersisted(testName);
        TriggerBuilder builder = Trigger.builder()
            .portalEnvironmentId(portalEnvironment.getId())
            .triggerType(TriggerType.EVENT)
            .deliveryType(NotificationDeliveryType.EMAIL);
        return buildPersisted(builder, null, portalEnvironment.getId());
    }
}
