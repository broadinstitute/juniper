package bio.terra.pearl.core.dao;

import bio.terra.pearl.core.model.StudyEnvironmentConfig;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class StudyEnvironmentConfigDao extends BaseJdbiDao<StudyEnvironmentConfig> {
    public StudyEnvironmentConfigDao(Jdbi jdbi) {
        super(jdbi);
    }
    @Override
    protected Class<StudyEnvironmentConfig> getClazz() {
        return StudyEnvironmentConfig.class;
    }

    public void deleteByStudyEnvironmentId(UUID studyEnvId) {
        deleteByUuidProperty("study_environment_id", studyEnvId);
    }
}
