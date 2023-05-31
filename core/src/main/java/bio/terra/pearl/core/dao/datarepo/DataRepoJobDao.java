package bio.terra.pearl.core.dao.datarepo;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.datarepo.DataRepoJob;
import bio.terra.pearl.core.model.datarepo.Dataset;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class DataRepoJobDao extends BaseMutableJdbiDao<DataRepoJob> {
    public DataRepoJobDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<DataRepoJob> getClazz() {
        return DataRepoJob.class;
    }

    public List<DataRepoJob> findAllByStatus(String status) {
        return findAllByProperty("status", status);
    }

    public List<DataRepoJob> findByDatasetId(UUID datasetId) {
        return findAllByProperty("dataset_id", datasetId);
    }

    public void updateJobStatus(UUID id, String newJobStatus) {
        updateProperty(id, "status", newJobStatus);
    }

    public void deleteByStudyEnvironmentId(UUID studyEnvId) {
        deleteByProperty("study_environment_id", studyEnvId);
    }
}
