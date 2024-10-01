package bio.terra.pearl.core.dao.export;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.export.ExportIntegration;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class ExportIntegrationDao extends BaseMutableJdbiDao<ExportIntegration> {
    public ExportIntegrationDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<ExportIntegration> getClazz() {
        return ExportIntegration.class;
    }

    public List<ExportIntegration> findByStudyEnvironmentId(UUID studyEnvId) {
        return findAllByProperty("study_environment_id", studyEnvId);
    }

    public void deleteByStudyEnvironmentId(UUID studyEnvId) {
        deleteByProperty("study_environment_id", studyEnvId);
    }
}
