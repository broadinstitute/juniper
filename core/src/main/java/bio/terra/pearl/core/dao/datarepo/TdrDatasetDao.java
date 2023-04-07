package bio.terra.pearl.core.dao.datarepo;

import bio.terra.pearl.core.dao.BaseJdbiDao;

import bio.terra.pearl.core.model.datarepo.TdrDataset;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class TdrDatasetDao extends BaseJdbiDao<TdrDataset> {
    public TdrDatasetDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<TdrDataset> getClazz() {
        return TdrDataset.class;
    }

}
