package bio.terra.pearl.core.dao;

import bio.terra.pearl.core.model.Environment;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class EnvironmentDao extends BaseJdbiDao<Environment> {
    @Override
    protected Class<Environment> getClazz() {
        return Environment.class;
    }

    public EnvironmentDao(Jdbi jdbi) {
        super(jdbi);
    }
}
