package bio.terra.pearl.core.dao;

import bio.terra.pearl.core.model.StudyEnvironment;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class StudyEnvironmentDao extends BaseJdbiDao<StudyEnvironment> {
    public StudyEnvironmentDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    public Class<StudyEnvironment> getClazz() {
        return StudyEnvironment.class;
    }
}
