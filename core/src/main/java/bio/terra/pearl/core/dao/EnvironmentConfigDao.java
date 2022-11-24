package bio.terra.pearl.core.dao;

import bio.terra.pearl.core.model.EnvironmentConfig;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class EnvironmentConfigDao extends BaseJdbiDao<EnvironmentConfig> {
    public EnvironmentConfigDao(Jdbi jdbi) {
        super(jdbi);
    }
    @Override
    protected Class<EnvironmentConfig> getClazz() {
        return EnvironmentConfig.class;
    }
}
