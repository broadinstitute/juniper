package bio.terra.pearl.core.dao.notification;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.notification.NotificationConfig;
import java.util.List;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class NotificationConfigDao extends BaseMutableJdbiDao<NotificationConfig> {
    public NotificationConfigDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<NotificationConfig> getClazz() {
        return NotificationConfig.class;
    }

    public List<NotificationConfig> findByStudyEnvironmentId(UUID studyEnvironmentId) {
        return findAllByProperty("study_environment_id", studyEnvironmentId);
    }
}
