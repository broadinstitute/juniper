package bio.terra.pearl.core.dao;

import bio.terra.pearl.core.model.PortalEnvironment;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class PortalEnvironmentDao extends BaseJdbiDao<PortalEnvironment> {
    public PortalEnvironmentDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    public Class<PortalEnvironment> getClazz() {
        return PortalEnvironment.class;
    }

}
