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

    public List<NotificationConfig> findByStudyEnvironmentId(UUID studyEnvironmentId, boolean active) {
        return dao.findByStudyEnvironmentId(studyEnvironmentId, active);
    }

    /** gets configs unaffiliated with a study */
    public List<NotificationConfig> findByPortalEnvironmentId(UUID portalEnvId) {
        return dao.findByPortalEnvironmentId(portalEnvId);
    }

    public void attachTemplates(List<NotificationConfig> configs) {
        dao.attachTemplates(configs);
    }

    public void deleteByStudyEnvironmentId(UUID studyEnvironmentId) {
        dao.deleteByStudyEnvironmentId(studyEnvironmentId);
    }

    public void deleteByPortalEnvironmentId(UUID portalEnvironmentId) {
        dao.deleteByPortalEnvironmentId(portalEnvironmentId);
    }
}
