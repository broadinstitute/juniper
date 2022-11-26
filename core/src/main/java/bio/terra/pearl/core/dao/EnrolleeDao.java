package bio.terra.pearl.core.dao;

import bio.terra.pearl.core.model.Enrollee;
import bio.terra.pearl.core.service.CascadeTree;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class EnrolleeDao extends BaseJdbiDao<Enrollee> {
    public EnrolleeDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<Enrollee> getClazz() {
        return Enrollee.class;
    }

    public void deleteByStudyEnvironmentId(UUID studyEnvironmentId, CascadeTree cascade) {
        deleteByUuidProperty("study_environment_id", studyEnvironmentId);
    }
}
