package bio.terra.pearl.core.service.notification;

import bio.terra.pearl.core.dao.notification.NotificationConfigDao;
import bio.terra.pearl.core.model.notification.NotificationConfig;
import bio.terra.pearl.core.service.CrudService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class NotificationConfigService extends CrudService<NotificationConfig, NotificationConfigDao> {
    public NotificationConfigService(NotificationConfigDao dao) {
        super(dao);
    }

    public List<NotificationConfig> findByStudyEnvironmentId(UUID studyEnvironmentId) {
        return dao.findByStudyEnvironmentId(studyEnvironmentId);
    }
}
