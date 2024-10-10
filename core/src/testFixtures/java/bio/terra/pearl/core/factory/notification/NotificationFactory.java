package bio.terra.pearl.core.factory.notification;

import bio.terra.pearl.core.factory.participant.EnrolleeBundle;
import bio.terra.pearl.core.model.notification.Notification;
import bio.terra.pearl.core.model.notification.Trigger;
import bio.terra.pearl.core.model.notification.NotificationDeliveryStatus;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.service.notification.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationFactory {
    @Autowired
    private NotificationService notificationService;
    public Notification.NotificationBuilder builder(EnrolleeBundle enrolleeBundle,
                                                    Trigger config) {
        Enrollee enrollee = enrolleeBundle.enrollee();
        return Notification.builder()
                .enrolleeId(enrollee.getId())
                .participantUserId(enrollee.getParticipantUserId())
                .triggerId(config.getId())
                .deliveryStatus(NotificationDeliveryStatus.READY)
                .deliveryType(config.getDeliveryType())
                .studyEnvironmentId(enrollee.getStudyEnvironmentId())
                .portalEnvironmentId(enrolleeBundle.portalParticipantUser().getPortalEnvironmentId())
                .retries(0);
    }

    public Notification buildPersisted(EnrolleeBundle enrolleeBundle,
                                       Trigger config) {
        return notificationService.create(builder(enrolleeBundle, config).build());
    }

    public Notification buildPersisted(Notification.NotificationBuilder builder) {
        return notificationService.create(builder.build());
    }
}
