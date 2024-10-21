package bio.terra.pearl.core.dao.notification;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.notification.SendgridEvent;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class SendgridEventDao extends BaseMutableJdbiDao<SendgridEvent> {

    public SendgridEventDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<SendgridEvent> getClazz() {
        return SendgridEvent.class;
    }

    public void bulkUpsert(List<SendgridEvent> activityLogs) {
        bulkUpsert(activityLogs, "msg_id");
    }

    public Optional<SendgridEvent> findMostRecentEvent() {
        return jdbi.withHandle(handle ->
                handle.createQuery("select * from " + tableName + " order by last_event_time desc limit 1")
                        .mapTo(clazz)
                        .findOne()
        );
    }

    public Optional<SendgridEvent> findByNotificationId(UUID notificationId) {
        return findByProperty("notification_id", notificationId);
    }

    public void deleteByNotificationId(UUID notificationId) {
        deleteByProperty("notification_id", notificationId);
    }

    public List<SendgridEvent> findByTriggerId(UUID triggerId) {
        return jdbi.withHandle(handle ->
                handle.createQuery(
                                "select sge.* from " + tableName + " sge " +
                                        " inner join notification n on n.id = sge.notification_id " +
                                        " inner join trigger t on t.id = n.trigger_id " +
                                        " where t.id = :triggerId")
                        .bind("triggerId", triggerId)
                        .mapTo(clazz)
                        .list()
        );
    }
}
