package bio.terra.pearl.core.dao.datarepo;

import bio.terra.pearl.core.dao.BaseJdbiDao;

import bio.terra.pearl.core.model.datarepo.Dataset;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class DatasetDao extends BaseJdbiDao<Dataset> {
    public DatasetDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<Dataset> getClazz() {
        return Dataset.class;
    }

}
