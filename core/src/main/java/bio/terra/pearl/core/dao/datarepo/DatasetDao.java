package bio.terra.pearl.core.dao.datarepo;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.datarepo.Dataset;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class DatasetDao extends BaseMutableJdbiDao<Dataset> {
    public DatasetDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<Dataset> getClazz() {
        return Dataset.class;
    }

    public List<Dataset> findByStudyEnvironmentId(UUID studyEnvId) {
        return findAllByProperty("study_environment_id", studyEnvId);
    }

    public Optional<Dataset> findByDataRepoId(UUID dataRepoId) {
        return findByProperty("dataset_id", dataRepoId);
    }

    public Optional<Dataset> findByDatasetName(String datasetName) {
        return findByProperty("dataset_name", datasetName);
    }

    public void updateLastExported(UUID id, Instant lastExported) {
        updateProperty(id, "last_exported", lastExported);
    }
}
