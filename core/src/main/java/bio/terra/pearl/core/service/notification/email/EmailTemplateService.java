package bio.terra.pearl.core.service.notification.email;

import bio.terra.pearl.core.dao.notification.EmailTemplateDao;
import bio.terra.pearl.core.model.notification.EmailTemplate;
import bio.terra.pearl.core.service.ImmutableEntityService;
import bio.terra.pearl.core.service.VersionedEntityService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class EmailTemplateService extends VersionedEntityService<EmailTemplate, EmailTemplateDao> {
    public EmailTemplateService(EmailTemplateDao dao) {
        super(dao);
    }

    public List<EmailTemplate> findByPortalId(UUID portalId) {
        return dao.findByPortalId(portalId);
    }

    public void deleteByPortalId(UUID portalId) {
        dao.deleteByPortalId(portalId);
    }
}
