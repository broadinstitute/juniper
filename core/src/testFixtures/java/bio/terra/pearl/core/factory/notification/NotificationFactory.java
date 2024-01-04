package bio.terra.pearl.core.factory.notification;

import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.model.notification.Notification;
import bio.terra.pearl.core.model.notification.TriggeredAction;
import bio.terra.pearl.core.model.notification.NotificationDeliveryStatus;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.service.notification.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationFactory {
    @Autowired
    private NotificationService notificationService;
    public Notification.NotificationBuilder builder(EnrolleeFactory.EnrolleeBundle enrolleeBundle,
                                                    TriggeredAction config) {
        Enrollee enrollee = enrolleeBundle.enrollee();
        return Notification.builder()
                .enrolleeId(enrollee.getId())
                .participantUserId(enrollee.getParticipantUserId())
                .notificationConfigId(config.getId())
                .deliveryStatus(NotificationDeliveryStatus.READY)
                .deliveryType(config.getDeliveryType())
                .studyEnvironmentId(enrollee.getStudyEnvironmentId())
                .portalEnvironmentId(enrolleeBundle.portalParticipantUser().getPortalEnvironmentId())
                .retries(0);
    }

    public Notification buildPersisted(EnrolleeFactory.EnrolleeBundle enrolleeBundle,
                                       TriggeredAction config) {
        return notificationService.create(builder(enrolleeBundle, config).build());
    }

    public Notification buildPersisted(Notification.NotificationBuilder builder) {
        return notificationService.create(builder.build());
    }
}
