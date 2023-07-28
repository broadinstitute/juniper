package bio.terra.pearl.core.dao.portal;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.portal.MailingListContact;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class MailingListContactDao extends BaseJdbiDao<MailingListContact> {
    public MailingListContactDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<MailingListContact> getClazz() {
        return MailingListContact.class;
    }

    public List<MailingListContact> findByPortalEnv(UUID portalEnvId) {
        return findAllByProperty("portal_environment_id", portalEnvId);
    }

    public Optional<MailingListContact> findByPortalEnv(UUID portalEnvId, String emailAddress) {
        return findByTwoProperties("portal_environment_id", portalEnvId, "email", emailAddress);
    }

    public void deleteByPortalEnvId(UUID portalEnvId) {
        deleteByProperty("portal_environment_id", portalEnvId);
    }
}
