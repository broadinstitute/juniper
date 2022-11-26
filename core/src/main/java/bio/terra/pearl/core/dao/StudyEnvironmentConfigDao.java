package bio.terra.pearl.core.dao;

import bio.terra.pearl.core.model.StudyEnvironmentConfig;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class StudyEnvironmentConfigDao extends BaseJdbiDao<StudyEnvironmentConfig> {
    public StudyEnvironmentConfigDao(Jdbi jdbi) {
        super(jdbi);
    }
    @Override
    protected Class<StudyEnvironmentConfig> getClazz() {
        return StudyEnvironmentConfig.class;
    }
}
