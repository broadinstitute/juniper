package bio.terra.pearl.core.dao.notification;

import bio.terra.pearl.core.dao.BaseVersionedJdbiDao;
import bio.terra.pearl.core.model.notification.EmailTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class EmailTemplateDao extends BaseVersionedJdbiDao<EmailTemplate> {
    public EmailTemplateDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<EmailTemplate> getClazz() {
        return EmailTemplate.class;
    }

    public Optional<EmailTemplate> findByStableId(String stableId, int version) {
        return findByTwoProperties("stable_id", stableId, "version", version);
    }

    public List<EmailTemplate> findByPortalId(UUID portalId) {
        return findAllByProperty("portal_id", portalId);
    }

    public void deleteByPortalId(UUID portalId) {
        deleteByProperty("portal_id", portalId);
    }
}
