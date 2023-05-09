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

    //todo: probably not necessary to check by both properties, but while "create your own dataset" is in development this feels safer.
    public List<DataRepoJob> findByStudyEnvironmentIdAndName(UUID studyEnvId, String datasetName) {
        return findAllByTwoProperties("study_environment_id", studyEnvId, "dataset_name", datasetName);
    }

    public List<DataRepoJob> findByStudyEnvironmentId(UUID studyEnvId) {
        return findAllByProperty("study_environment_id", studyEnvId);
    }

    public void updateJobStatus(UUID id, String newJobStatus) {
        updateProperty(id, "status", newJobStatus);
    }

    public void deleteByStudyEnvironmentId(UUID studyEnvId) {
        deleteByProperty("study_environment_id", studyEnvId);
    }
}
