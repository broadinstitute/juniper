package bio.terra.pearl.core.service.notification;

import bio.terra.pearl.core.dao.notification.TriggerDao;
import bio.terra.pearl.core.model.notification.EmailTemplate;
import bio.terra.pearl.core.model.notification.Trigger;
import bio.terra.pearl.core.service.CrudService;
import bio.terra.pearl.core.service.notification.email.EmailTemplateService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class TriggerService extends CrudService<Trigger, TriggerDao> {
    private EmailTemplateService emailTemplateService;

    public TriggerService(TriggerDao dao, EmailTemplateService emailTemplateService) {
        super(dao);
        this.emailTemplateService = emailTemplateService;
    }

    public List<Trigger> findByStudyEnvironmentId(UUID studyEnvironmentId) {
        return dao.findByStudyEnvironmentId(studyEnvironmentId);
    }

    public List<Trigger> findByStudyEnvironmentId(UUID studyEnvironmentId, boolean active) {
        return dao.findByStudyEnvironmentId(studyEnvironmentId, active);
    }

    /** gets configs unaffiliated with a study */
    public List<Trigger> findByPortalEnvironmentId(UUID portalEnvId) {
        return dao.findByPortalEnvironmentId(portalEnvId);
    }

    @Override
    public Trigger create(Trigger action) {
        EmailTemplate emailTemplate = action.getEmailTemplate();
        if (emailTemplate != null && emailTemplate.getId() == null) {
            emailTemplate = emailTemplateService.create(emailTemplate);
            action.setEmailTemplateId(emailTemplate.getId());
        }
        Trigger savedConfig = dao.create(action);
        savedConfig.setEmailTemplate(emailTemplate);
        return savedConfig;
    }

    public void attachTemplates(List<Trigger> actions) {
        dao.attachTemplates(actions);
    }

    public void deleteByStudyEnvironmentId(UUID studyEnvironmentId) {
        dao.deleteByStudyEnvironmentId(studyEnvironmentId);
    }

    public void deleteByPortalEnvironmentId(UUID portalEnvironmentId) {
        dao.deleteByPortalEnvironmentId(portalEnvironmentId);
    }
}
