package bio.terra.pearl.core.dao.notification;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

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
}
