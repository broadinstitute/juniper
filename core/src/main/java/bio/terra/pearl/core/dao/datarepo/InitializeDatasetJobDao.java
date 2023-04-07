package bio.terra.pearl.core.dao.datarepo;

import bio.terra.pearl.core.dao.BaseJdbiDao;

import bio.terra.pearl.core.model.datarepo.InitializeDatasetJob;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class InitializeDatasetJobDao extends BaseJdbiDao<InitializeDatasetJob> {
    public InitializeDatasetJobDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<InitializeDatasetJob> getClazz() {
        return InitializeDatasetJob.class;
    }

    public List<InitializeDatasetJob> findAllByStatus(String status) {
        return findAllByProperty("status", status);
    }

    public void updateJobStatus(UUID id, String oldJobStatus, String newJobStatus) {
        updateProperty(id, "status", oldJobStatus, newJobStatus);
    }

}
