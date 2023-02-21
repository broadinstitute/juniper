package bio.terra.pearl.core.dao.notification;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.notification.EmailTemplate;
import bio.terra.pearl.core.model.notification.NotificationConfig;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang3.NotImplementedException;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class NotificationConfigDao extends BaseMutableJdbiDao<NotificationConfig> {
    private EmailTemplateDao emailTemplateDao;
    public NotificationConfigDao(Jdbi jdbi, EmailTemplateDao emailTemplateDao) {
        super(jdbi);
        this.emailTemplateDao = emailTemplateDao;
    }

    @Override
    protected Class<NotificationConfig> getClazz() {
        return NotificationConfig.class;
    }

    public List<NotificationConfig> findByStudyEnvironmentId(UUID studyEnvironmentId) {
        return findAllByProperty("study_environment_id", studyEnvironmentId);
    }

    public List<NotificationConfig> findByStudyEnvironmentId(UUID studyEnvironmentId, boolean active) {
        return findAllByTwoProperties("study_environment_id", studyEnvironmentId, "active", active);
    }

    public List<NotificationConfig> findByStudyEnvWithTemplates(UUID studyEnvironmentId, boolean active) {
        List<NotificationConfig> configs = findByStudyEnvironmentId(studyEnvironmentId, active);
        List<UUID> templateIds = configs.stream().map(NotificationConfig::getEmailTemplateId)
                .filter(uuid -> uuid != null).toList();
        List<EmailTemplate> templates = emailTemplateDao.findAll(templateIds);
        for (NotificationConfig config : configs) {
            config.setEmailTemplate(templates.stream()
                    .filter(template -> template.getId().equals(config.getEmailTemplateId()))
                    .findFirst().orElse(null)
            );
        }
        return configs;
    }

    @Override
    public NotificationConfig update(NotificationConfig config) {
        throw new NotImplementedException("only updateActive is supported");
    }

    public void updateActive(UUID notificationConfigId, boolean active) {
        updateProperty(notificationConfigId, "active", active);
    }

    public void deleteByStudyEnvironmentId(UUID studyEnvironmentId) {
        deleteByUuidProperty("study_environment_id", studyEnvironmentId);
    }
}
