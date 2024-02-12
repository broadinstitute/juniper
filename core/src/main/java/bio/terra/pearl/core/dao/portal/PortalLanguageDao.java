package bio.terra.pearl.core.dao.portal;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.portal.PortalEnvironmentLanguage;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class PortalLanguageDao extends BaseJdbiDao<PortalEnvironmentLanguage> {

    @Override
    public Class<PortalEnvironmentLanguage> getClazz() {
        return PortalEnvironmentLanguage.class;
    }

    public PortalLanguageDao(Jdbi jdbi) {
        super(jdbi);
    }

    public List<PortalEnvironmentLanguage> findByPortalEnvId(UUID portalId) {
        return findAllByProperty("portal_environment_id", portalId);
    }
}
