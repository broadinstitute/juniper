package bio.terra.pearl.core.service.notification.email;

import bio.terra.pearl.core.dao.notification.EmailTemplateDao;
import bio.terra.pearl.core.model.notification.EmailTemplate;
import bio.terra.pearl.core.service.ImmutableEntityService;
import bio.terra.pearl.core.service.VersionedEntityService;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class EmailTemplateService extends ImmutableEntityService<EmailTemplate, EmailTemplateDao> implements VersionedEntityService<EmailTemplate> {
    public EmailTemplateService(EmailTemplateDao dao) {
        super(dao);
    }

    public Optional<EmailTemplate> findByStableId(String stableId, int version) {
        return dao.findByStableId(stableId, version);
    }

    public int getNextVersion(String stableId) {
        return dao.getNextVersion(stableId);
    }

    public void deleteByPortalId(UUID portalId) {
        dao.deleteByPortalId(portalId);
    }
}
