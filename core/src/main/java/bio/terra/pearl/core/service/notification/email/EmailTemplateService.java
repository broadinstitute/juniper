package bio.terra.pearl.core.service.notification.email;

import bio.terra.pearl.core.dao.notification.EmailTemplateDao;
import bio.terra.pearl.core.model.notification.EmailTemplate;
import bio.terra.pearl.core.model.notification.LocalizedEmailTemplate;
import bio.terra.pearl.core.service.ImmutableEntityService;
import bio.terra.pearl.core.service.VersionedEntityService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class EmailTemplateService extends VersionedEntityService<EmailTemplate, EmailTemplateDao> {
    private final LocalizedEmailTemplateService localizedEmailTemplateService;
    public EmailTemplateService(EmailTemplateDao dao, LocalizedEmailTemplateService localizedEmailTemplateService) {
        super(dao);
        this.localizedEmailTemplateService = localizedEmailTemplateService;
    }

    public List<EmailTemplate> findByPortalId(UUID portalId) {
        return dao.findByPortalId(portalId);
    }

    @Override
    public EmailTemplate create(EmailTemplate emailTemplate) {
        EmailTemplate template = dao.create(emailTemplate);
        for (LocalizedEmailTemplate localizedEmailTemplate : emailTemplate.getLocalizedEmailTemplates()) {
            localizedEmailTemplate.setEmailTemplateId(template.getId());
            LocalizedEmailTemplate savedTemplate = localizedEmailTemplateService.create(localizedEmailTemplate);
            template.getLocalizedEmailTemplates().add(savedTemplate);
        }
        return template;
    }

    public EmailTemplate attachLocalizedTemplate(EmailTemplate emailTemplate, String language) {
        dao.attachLocalizedTemplate(emailTemplate, language);
        return emailTemplate;
    }

    public EmailTemplate attachLocalizedTemplates(EmailTemplate emailTemplate) {
        dao.attachLocalizedTemplates(emailTemplate);
        return emailTemplate;
    }

    public void deleteByPortalId(UUID portalId) {
        dao.deleteByPortalId(portalId);
    }
}
