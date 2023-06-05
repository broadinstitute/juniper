package bio.terra.pearl.core.service.notification;

import bio.terra.pearl.core.dao.notification.NotificationConfigDao;
import bio.terra.pearl.core.model.notification.EmailTemplate;
import bio.terra.pearl.core.model.notification.NotificationConfig;
import bio.terra.pearl.core.service.CrudService;
import bio.terra.pearl.core.service.notification.email.EmailTemplateService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class NotificationConfigService extends CrudService<NotificationConfig, NotificationConfigDao> {
    private EmailTemplateService emailTemplateService;

    public NotificationConfigService(NotificationConfigDao dao, EmailTemplateService emailTemplateService) {
        super(dao);
        this.emailTemplateService = emailTemplateService;
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

    @Override
    public NotificationConfig create(NotificationConfig config) {
        EmailTemplate emailTemplate = config.getEmailTemplate();
        if (emailTemplate != null && emailTemplate.getId() == null) {
            emailTemplate = emailTemplateService.create(emailTemplate);
            config.setEmailTemplateId(emailTemplate.getId());
        }
        NotificationConfig savedConfig = dao.create(config);
        savedConfig.setEmailTemplate(emailTemplate);
        return savedConfig;
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
