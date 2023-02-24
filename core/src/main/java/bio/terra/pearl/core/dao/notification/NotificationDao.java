package bio.terra.pearl.core.dao.notification;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.notification.Notification;
import java.util.List;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class NotificationDao extends BaseMutableJdbiDao<Notification> {
    public NotificationDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<Notification> getClazz() {
        return Notification.class;
    }

    public List<Notification> findByEnrolleeId(UUID enrolleeId) {
        return findAllByProperty("enrollee_id", enrolleeId);
    }

    public void deleteByEnrolleeId(UUID enrolleeId) {
        deleteByProperty("enrollee_id", enrolleeId);
    }
}
