package bio.terra.pearl.core.dao.study;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.study.StudyEnvironmentConfig;
import java.util.List;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class StudyEnvironmentConfigDao extends BaseMutableJdbiDao<StudyEnvironmentConfig> {
    public StudyEnvironmentConfigDao(Jdbi jdbi) {
        super(jdbi);
    }
    @Override
    protected Class<StudyEnvironmentConfig> getClazz() {
        return StudyEnvironmentConfig.class;
    }

    public void deleteByStudyEnvironmentId(UUID studyEnvId) {
        deleteByProperty("study_environment_id", studyEnvId);
    }

    public List<StudyEnvironmentConfig> findAll(List<UUID> configIds) {
        return findAllByPropertyCollection("id", configIds);
    }
}
