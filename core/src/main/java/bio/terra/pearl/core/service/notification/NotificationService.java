package bio.terra.pearl.core.service.notification;

import bio.terra.pearl.core.dao.notification.NotificationDao;
import bio.terra.pearl.core.dao.participant.EnrolleeDao;
import bio.terra.pearl.core.model.notification.Notification;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.service.CrudService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class NotificationService extends CrudService<Notification, NotificationDao> {
    private EnrolleeDao enrolleeDao;

    public NotificationService(
            NotificationDao dao,
            EnrolleeDao enrolleeDao) {
        super(dao);
        this.enrolleeDao = enrolleeDao;
    }

    public List<Notification> findByEnrolleeId(UUID enrolleeId) {
        return dao.findByEnrolleeId(enrolleeId);
    }
    public void deleteByEnrolleeId(UUID enrolleeId) {
        dao.deleteByEnrolleeId(enrolleeId);
    }

    public List<Notification> findAllByConfigId(UUID configId, boolean withEnrollees) {
        List<Notification> notifications = dao.findAllByConfigId(configId);

        if (withEnrollees) {
            attachEnrollees(notifications);
        }

        return notifications;
    }

    private void attachEnrollees(List<Notification> notifications) {
        List<UUID> enrolleeIds = notifications
                .stream()
                .map(Notification::getEnrolleeId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<UUID, Enrollee> enrollees = enrolleeDao
                .findAll(enrolleeIds)
                .stream()
                .collect(Collectors.toMap(Enrollee::getId, enrollee -> enrollee));

        notifications.forEach(notification -> {
            if (Objects.nonNull(notification.getEnrolleeId())) {
                notification.setEnrollee(enrollees.get(notification.getEnrolleeId()));
            }
        });
    }
}
