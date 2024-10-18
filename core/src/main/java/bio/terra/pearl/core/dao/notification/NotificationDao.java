package bio.terra.pearl.core.dao.notification;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.notification.Notification;
import bio.terra.pearl.core.model.notification.SendgridEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class NotificationDao extends BaseMutableJdbiDao<Notification> {
    private ObjectMapper objectMapper;
    private final SendgridEventDao sendgridEventDao;

    public NotificationDao(Jdbi jdbi, ObjectMapper objectMapper, SendgridEventDao sendgridEventDao) {
        super(jdbi);
        this.objectMapper = objectMapper;
        this.sendgridEventDao = sendgridEventDao;
    }

    @Override
    protected Class<Notification> getClazz() {
        return Notification.class;
    }

    /** handles serializing the message map, if it exists */
    @Override
    public Notification create(Notification notification) {
        if (notification.getCustomMessagesMap() != null && !notification.getCustomMessagesMap().isEmpty()) {
            try {
                notification.setCustomMessages(objectMapper.writeValueAsString(notification.getCustomMessagesMap()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Could not serialize custom messages", e);
            }
        }
        return super.create(notification);
    }

    public List<Notification> findByEnrolleeId(UUID enrolleeId) {
        return findAllByProperty("enrollee_id", enrolleeId);
    }

    public void attachSendgridEvent(Notification notification) {
        Optional<SendgridEvent> sendgridEvent = sendgridEventDao.findByNotificationId(notification.getId());
        sendgridEvent.ifPresent(notification::setEventDetails);
    }

    public List<Notification> findAllBySendgridApiRequestId(List<String> apiRequestIds) {
        return findAllByPropertyCollection("sendgrid_api_request_id", apiRequestIds);
    }

    public void deleteByEnrolleeId(UUID enrolleeId) {
        deleteByProperty("enrollee_id", enrolleeId);
    }

    public List<Notification> findAllByConfigId(UUID configId) {
        return findAllByProperty("trigger_id", configId);
    }
}
