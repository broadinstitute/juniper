package bio.terra.pearl.core.dao.notification;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.notification.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

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
        return jdbi.withHandle(handle ->
                handle.createQuery(
                                "SELECT " +
                                        "n.id as notification_id, n.enrollee_id, n.participant_user_id, n.portal_environment_id, n.study_environment_id, n.trigger_id, n.delivery_status, n.delivery_type, n.sent_to, n.custom_messages, n.sendgrid_api_request_id, n.retries, " +
                                        "se.msg_id as msg_id, se.subject, se.to_email, se.from_email, se.status, se.opens_count, se.clicks_count, se.last_event_time, se.api_request_id " +
                                        "FROM notification n " +
                                        "LEFT JOIN sendgrid_event se ON n.sendgrid_api_request_id = se.api_request_id AND n.sent_to = se.to_email " +
                                        "WHERE n.enrollee_id = :enrolleeId"
                        )
                        .bind("enrolleeId", enrolleeId)
                        .map((rs, ctx) -> {
                            Notification notification = new Notification();
                            notification.setId(UUID.fromString(rs.getString("notification_id")));
                            notification.setEnrolleeId(UUID.fromString(rs.getString("enrollee_id")));
                            notification.setParticipantUserId(UUID.fromString(rs.getString("participant_user_id")));
                            notification.setPortalEnvironmentId(UUID.fromString(rs.getString("portal_environment_id")));
                            notification.setStudyEnvironmentId(UUID.fromString(rs.getString("study_environment_id")));
                            notification.setTriggerId(UUID.fromString(rs.getString("trigger_id")));
                            notification.setDeliveryStatus(NotificationDeliveryStatus.valueOf(rs.getString("delivery_status")));
                            notification.setDeliveryType(NotificationDeliveryType.valueOf(rs.getString("delivery_type")));
                            notification.setSentTo(rs.getString("sent_to"));
                            notification.setCustomMessages(rs.getString("custom_messages"));
                            notification.setRetries(rs.getInt("retries"));

                            if (rs.getString("msg_id") != null) {
                                NotificationEventDetails notificationEventDetails = new NotificationEventDetails();
                                notificationEventDetails.setSubject(rs.getString("subject"));
                                notificationEventDetails.setToEmail(rs.getString("to_email"));
                                notificationEventDetails.setFromEmail(rs.getString("from_email"));
                                notificationEventDetails.setStatus(rs.getString("status"));
                                notificationEventDetails.setOpensCount(rs.getInt("opens_count"));
                                notificationEventDetails.setClicksCount(rs.getInt("clicks_count"));
                                notificationEventDetails.setLastEventTime(rs.getTimestamp("last_event_time").toInstant());

                                notification.setNotificationEventDetails(notificationEventDetails);
                            }

                            return notification;
                        })
                        .list()
        );
    }

    public void deleteByEnrolleeId(UUID enrolleeId) {
        deleteByProperty("enrollee_id", enrolleeId);
    }
}
