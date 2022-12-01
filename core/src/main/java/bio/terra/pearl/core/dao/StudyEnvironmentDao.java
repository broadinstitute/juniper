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
        return jdbi.withHandle(handle ->
                handle.createQuery("select * from " + tableName + " where study_id = :studyId;")
                        .bind("studyId", studyId)
                        .mapTo(clazz)
                        .list()
        );
    }
}
