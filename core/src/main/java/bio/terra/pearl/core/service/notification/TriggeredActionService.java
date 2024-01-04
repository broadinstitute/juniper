package bio.terra.pearl.core.service.notification;

import bio.terra.pearl.core.dao.notification.TriggeredActionDao;
import bio.terra.pearl.core.model.notification.EmailTemplate;
import bio.terra.pearl.core.model.notification.TriggeredAction;
import bio.terra.pearl.core.service.CrudService;
import bio.terra.pearl.core.service.notification.email.EmailTemplateService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class TriggeredActionService extends CrudService<TriggeredAction, TriggeredActionDao> {
    private EmailTemplateService emailTemplateService;

    public TriggeredActionService(TriggeredActionDao dao, EmailTemplateService emailTemplateService) {
        super(dao);
        this.emailTemplateService = emailTemplateService;
    }

    public List<TriggeredAction> findByStudyEnvironmentId(UUID studyEnvironmentId) {
        return dao.findByStudyEnvironmentId(studyEnvironmentId);
    }

    public List<TriggeredAction> findByStudyEnvironmentId(UUID studyEnvironmentId, boolean active) {
        return dao.findByStudyEnvironmentId(studyEnvironmentId, active);
    }

    /** gets configs unaffiliated with a study */
    public List<TriggeredAction> findByPortalEnvironmentId(UUID portalEnvId) {
        return dao.findByPortalEnvironmentId(portalEnvId);
    }

    @Override
    public TriggeredAction create(TriggeredAction action) {
        EmailTemplate emailTemplate = action.getEmailTemplate();
        if (emailTemplate != null && emailTemplate.getId() == null) {
            emailTemplate = emailTemplateService.create(emailTemplate);
            action.setEmailTemplateId(emailTemplate.getId());
        }
        TriggeredAction savedConfig = dao.create(action);
        savedConfig.setEmailTemplate(emailTemplate);
        return savedConfig;
    }

    public void attachTemplates(List<TriggeredAction> actions) {
        dao.attachTemplates(actions);
    }

    public void deleteByStudyEnvironmentId(UUID studyEnvironmentId) {
        dao.deleteByStudyEnvironmentId(studyEnvironmentId);
    }

    public void deleteByPortalEnvironmentId(UUID portalEnvironmentId) {
        dao.deleteByPortalEnvironmentId(portalEnvironmentId);
    }
}
