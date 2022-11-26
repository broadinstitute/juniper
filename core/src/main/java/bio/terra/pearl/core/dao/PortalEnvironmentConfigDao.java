package bio.terra.pearl.core.dao;

import bio.terra.pearl.core.model.PortalEnvironmentConfig;
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
