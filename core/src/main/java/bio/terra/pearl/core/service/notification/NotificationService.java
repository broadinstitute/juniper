package bio.terra.pearl.core.service.notification;

import bio.terra.pearl.core.dao.notification.NotificationDao;
import bio.terra.pearl.core.model.notification.Notification;
import bio.terra.pearl.core.service.CrudService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class NotificationService extends CrudService<Notification, NotificationDao> {
    private ObjectMapper objectMapper;
    public NotificationService(NotificationDao dao, ObjectMapper objectMapper) {
        super(dao);
        this.objectMapper = objectMapper;
    }

    public List<Notification> findByEnrolleeId(UUID enrolleeId) {
        List<Notification> notifications = dao.findByEnrolleeId(enrolleeId);
        for (Notification notification : notifications) {
            dao.attachSendgridEvent(notification);
        }
        return notifications;
    }

    public void deleteByEnrolleeId(UUID enrolleeId) {
        dao.deleteByEnrolleeId(enrolleeId);
    }
}
