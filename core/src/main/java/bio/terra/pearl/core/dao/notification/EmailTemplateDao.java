package bio.terra.pearl.core.dao.notification;

import bio.terra.pearl.core.dao.BaseVersionedJdbiDao;
import bio.terra.pearl.core.model.notification.EmailTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import bio.terra.pearl.core.model.notification.LocalizedEmailTemplate;
import bio.terra.pearl.core.service.exception.NotFoundException;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class EmailTemplateDao extends BaseVersionedJdbiDao<EmailTemplate> {
    private final LocalizedEmailTemplateDao localizedEmailTemplateDao;
    public EmailTemplateDao(Jdbi jdbi, LocalizedEmailTemplateDao localizedEmailTemplateDao) {
        super(jdbi);
        this.localizedEmailTemplateDao = localizedEmailTemplateDao;
    }

    @Override
    protected Class<EmailTemplate> getClazz() {
        return EmailTemplate.class;
    }

    public List<EmailTemplate> findAllWithLocalizedTemplates(List<UUID> templateIds) {
        List<EmailTemplate> emailTemplates = findAll(templateIds);
        for (EmailTemplate emailTemplate : emailTemplates) {
            emailTemplate.setLocalizedEmailTemplates(localizedEmailTemplateDao.findByEmailTemplate(emailTemplate.getId()));
        }
        return emailTemplates;
    }

    public EmailTemplate attachLocalizedTemplate(EmailTemplate emailTemplate, String language) {
        LocalizedEmailTemplate localizedEmailTemplate = localizedEmailTemplateDao.findByEmailTemplate(emailTemplate.getId(), language).orElseThrow(() -> new NotFoundException("LocalizedEmailTemplate not found for language " + language));
        emailTemplate.setLocalizedEmailTemplates(List.of(localizedEmailTemplate));
        return emailTemplate;
    }
    
    public EmailTemplate attachLocalizedTemplates(EmailTemplate emailTemplate) {
        emailTemplate.setLocalizedEmailTemplates(localizedEmailTemplateDao.findByEmailTemplate(emailTemplate.getId()));
        return emailTemplate;
    }

    public List<EmailTemplate> findByPortalId(UUID portalId) {
        return findAllByProperty("portal_id", portalId);
    }

    public void deleteByPortalId(UUID portalId) {
        deleteByProperty("portal_id", portalId);
    }
}
