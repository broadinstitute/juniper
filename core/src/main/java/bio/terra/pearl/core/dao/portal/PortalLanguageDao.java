package bio.terra.pearl.core.dao.portal;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.portal.PortalLanguage;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class PortalLanguageDao extends BaseJdbiDao<PortalLanguage> {

    @Override
    public Class<PortalLanguage> getClazz() {
        return PortalLanguage.class;
    }

    public PortalLanguageDao(Jdbi jdbi) {
        super(jdbi);
    }

    public List<PortalLanguage> findByPortalId(UUID portalId) {
        return findAllByProperty("portal_id", portalId);
    }
}
