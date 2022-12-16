package bio.terra.pearl.core.dao.study;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class StudyDao  extends BaseJdbiDao<Study> {
    private StudyEnvironmentDao studyEnvironmentDao;

    @Override
    protected Class<Study> getClazz() {
        return Study.class;
    }

    public StudyDao(Jdbi jdbi, StudyEnvironmentDao studyEnvironmentDao) {
        super(jdbi);
        this.studyEnvironmentDao = studyEnvironmentDao;
    }

    public Optional<Study> findOneByShortcode(String shortcode) {
        return findByProperty("shortcode", shortcode);
    }

    public Optional<Study> findOneFullLoad(UUID id) {
        Optional<Study> studyOpt = find(id);
        studyOpt.ifPresent(study -> {
            List<StudyEnvironment> studyEnvs = studyEnvironmentDao.findByStudy(id);
            for (StudyEnvironment studyEnv : studyEnvs) {
                study.getStudyEnvironments().add(studyEnv);
            }
        });
        return studyOpt;
    }
}
