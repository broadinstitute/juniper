package bio.terra.pearl.core.dao.notification;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.notification.Notification;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class NotificationDao extends BaseMutableJdbiDao<Notification> {
    private ObjectMapper objectMapper;
    public NotificationDao(Jdbi jdbi, ObjectMapper objectMapper) {
        super(jdbi);
        this.objectMapper = objectMapper;
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

    public void deleteByEnrolleeId(UUID enrolleeId) {
        deleteByProperty("enrollee_id", enrolleeId);
    }
}
