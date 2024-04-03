package bio.terra.pearl.core.service.notification;

import bio.terra.pearl.core.dao.notification.NotificationDao;
import bio.terra.pearl.core.dao.notification.SendgridEventDao;
import bio.terra.pearl.core.model.notification.Notification;
import bio.terra.pearl.core.service.CrudService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class NotificationService extends CrudService<Notification, NotificationDao> {
    private final ObjectMapper objectMapper;
    private final SendgridEventDao sendgridEventDao;
    public NotificationService(NotificationDao dao, ObjectMapper objectMapper, SendgridEventDao sendgridEventDao) {
        super(dao);
        this.objectMapper = objectMapper;
        this.sendgridEventDao = sendgridEventDao;
    }

    public List<Notification> findByEnrolleeId(UUID enrolleeId) {
        List<Notification> notifications = dao.findByEnrolleeId(enrolleeId);
        for (Notification notification : notifications) {
            dao.attachSendgridEvent(notification);
        }
        return notifications;
    }

    @Transactional
    public void deleteByEnrolleeId(UUID enrolleeId) {
        List<Notification> notifications = dao.findByEnrolleeId(enrolleeId);
        for (Notification notification : notifications) {
            sendgridEventDao.deleteByNotificationId(notification.getId());
        }
        dao.deleteByEnrolleeId(enrolleeId);
    }
}
