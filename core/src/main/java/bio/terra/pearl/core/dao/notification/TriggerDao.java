package bio.terra.pearl.core.dao.notification;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.dao.StudyEnvAttachedDao;
import bio.terra.pearl.core.model.notification.EmailTemplate;
import bio.terra.pearl.core.model.notification.Trigger;
import java.util.List;
import java.util.UUID;

import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class TriggerDao extends BaseMutableJdbiDao<Trigger> implements StudyEnvAttachedDao<Trigger> {
    private EmailTemplateDao emailTemplateDao;
    public TriggerDao(Jdbi jdbi, EmailTemplateDao emailTemplateDao) {
        super(jdbi);
        this.emailTemplateDao = emailTemplateDao;
    }

    @Override
    protected Class<Trigger> getClazz() {
        return Trigger.class;
    }

    public List<Trigger> findByStudyEnvironmentId(UUID studyEnvironmentId, boolean active) {
        return findAllByTwoProperties("study_environment_id", studyEnvironmentId, "active", active);
    }

    /** gets the configs for the portal environment that are unassociated with studies */
    public List<Trigger> findByPortalEnvironmentId(UUID portalEnvironmentId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("select * from " + tableName + " where portal_environment_id = :portalEnvironmentId" +
                                " and study_environment_id is null;")
                        .bind("portalEnvironmentId", portalEnvironmentId)
                        .mapTo(clazz)
                        .list()
        );
    }



    public void attachTemplates(List<Trigger> actions) {
        List<UUID> templateIds = actions.stream().map(Trigger::getEmailTemplateId)
                .filter(uuid -> uuid != null).toList();
        List<EmailTemplate> templates = emailTemplateDao.findAllWithLocalizedTemplates(templateIds);
        for (Trigger action : actions) {
            action.setEmailTemplate(templates.stream()
                    .filter(template -> template.getId().equals(action.getEmailTemplateId()))
                    .findFirst().orElse(null)
            );
        }
    }

    public void deleteByPortalEnvironmentId(UUID studyEnvironmentId) {
        deleteByProperty("portal_environment_id", studyEnvironmentId);
    }
}
