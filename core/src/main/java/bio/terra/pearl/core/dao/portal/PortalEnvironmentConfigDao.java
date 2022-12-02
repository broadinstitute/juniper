package bio.terra.pearl.core.dao.portal;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.portal.PortalEnvironmentConfig;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class PortalEnvironmentConfigDao extends BaseJdbiDao<PortalEnvironmentConfig> {
    public PortalEnvironmentConfigDao(Jdbi jdbi) {
        super(jdbi);
    }
    @Override
    protected Class<PortalEnvironmentConfig> getClazz() {
        return PortalEnvironmentConfig.class;
    }
}
