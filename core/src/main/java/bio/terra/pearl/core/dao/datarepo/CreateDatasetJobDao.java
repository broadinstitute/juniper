package bio.terra.pearl.core.dao.datarepo;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.datarepo.CreateDatasetJob;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class CreateDatasetJobDao extends BaseMutableJdbiDao<CreateDatasetJob> {
    public CreateDatasetJobDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<CreateDatasetJob> getClazz() {
        return CreateDatasetJob.class;
    }

    public List<CreateDatasetJob> findAllByStatus(String status) {
        return findAllByProperty("status", status);
    }

    public void updateJobStatus(UUID id, String newJobStatus) {
        updateProperty(id, "status", newJobStatus);
    }

    public void deleteByStudyEnvironmentId(UUID studyEnvId) {
        deleteByProperty("study_environment_id", studyEnvId);
    }
}
