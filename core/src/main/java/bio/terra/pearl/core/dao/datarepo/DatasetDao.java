package bio.terra.pearl.core.dao.datarepo;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.datarepo.Dataset;
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

    public void deleteByStudyEnvironmentId(UUID studyEnvId) {
        deleteByProperty("study_environment_id", studyEnvId);
    }
}
