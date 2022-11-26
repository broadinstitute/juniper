package bio.terra.pearl.core.dao;

import bio.terra.pearl.core.model.StudyEnvironment;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class StudyEnvironmentDao extends BaseJdbiDao<StudyEnvironment> {
    public StudyEnvironmentDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    public Class<StudyEnvironment> getClazz() {
        return StudyEnvironment.class;
    }

    public List<StudyEnvironment> findByStudy(UUID studyId) {
        return findAllByProperty("study_id", studyId);
    }

    public void deleteByStudyId(UUID studyId) {
        deleteByUuidProperty("study_id", studyId);
    }
}
