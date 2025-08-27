package bio.terra.pearl.core.dao.notification;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.notification.Notification;
import bio.terra.pearl.core.model.notification.SendgridEvent;
import bio.terra.pearl.core.model.notification.Trigger;
import bio.terra.pearl.core.model.participant.Enrollee;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class NotificationDao extends BaseMutableJdbiDao<Notification> {
    private final TriggerDao triggerDao;
    private final EmailTemplateDao emailTemplateDao;
    private ObjectMapper objectMapper;
    private final SendgridEventDao sendgridEventDao;

    public NotificationDao(Jdbi jdbi, ObjectMapper objectMapper, SendgridEventDao sendgridEventDao, TriggerDao triggerDao, EmailTemplateDao emailTemplateDao) {
        super(jdbi);
        this.objectMapper = objectMapper;
        this.sendgridEventDao = sendgridEventDao;
        this.triggerDao = triggerDao;
        this.emailTemplateDao = emailTemplateDao;
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

    public Optional<Notification> findMostRecentSentByEnrolleeAndTriggerId(UUID enrolleeId, UUID triggerId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                                SELECT * FROM notification
                                WHERE enrollee_id = :enrolleeId AND trigger_id = :triggerId
                                AND delivery_status = 'SENT'
                                ORDER BY created_at DESC LIMIT 1
                                """)
                        .bind("enrolleeId", enrolleeId)
                        .bind("triggerId", triggerId)
                        .mapTo(getClazz())
                        .findFirst()
        );
    }

    public List<Notification> findByEnrolleeAndEmailStableId(Enrollee enrollee, String stableId) {
        List<Trigger> triggers = triggerDao.findByStudyEnvAndEmailTemplate(enrollee.getStudyEnvironmentId(), stableId);
        return findAllByTwoProperties("enrollee_id", enrollee.getId(),
                "trigger_id", triggers.stream().map(Trigger::getId).toList());
    }

}
