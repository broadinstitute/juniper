package bio.terra.pearl.core.dao.notification;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.notification.EmailTemplate;
import bio.terra.pearl.core.model.notification.NotificationConfig;
import java.util.List;
import java.util.UUID;
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

    /** gets the configs for the portal environment that are unassociated with studies */
    public List<NotificationConfig> findByPortalEnvironmentId(UUID portalEnvironmentId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("select * from " + tableName + " where portal_environment_id = :portalEnvironmentId" +
                                " and study_environment_id is null;")
                        .bind("portalEnvironmentId", portalEnvironmentId)
                        .mapTo(clazz)
                        .list()
        );
    }



    public void attachTemplates(List<NotificationConfig> configs) {
        List<UUID> templateIds = configs.stream().map(NotificationConfig::getEmailTemplateId)
                .filter(uuid -> uuid != null).toList();
        List<EmailTemplate> templates = emailTemplateDao.findAll(templateIds);
        for (NotificationConfig config : configs) {
            config.setEmailTemplate(templates.stream()
                    .filter(template -> template.getId().equals(config.getEmailTemplateId()))
                    .findFirst().orElse(null)
            );
        }
    }

    public void deleteByStudyEnvironmentId(UUID studyEnvironmentId) {
        deleteByProperty("study_environment_id", studyEnvironmentId);
    }

    public void deleteByPortalEnvironmentId(UUID studyEnvironmentId) {
        deleteByProperty("portal_environment_id", studyEnvironmentId);
    }
}
