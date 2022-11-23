package bio.terra.pearl.core.service;

import bio.terra.pearl.core.dao.StudyEnvironmentDao;
import bio.terra.pearl.core.model.StudyEnvironment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class StudyEnvironmentService {
    private StudyEnvironmentDao studyEnvironmentDao;

    public StudyEnvironmentService(StudyEnvironmentDao studyEnvironmentDao) {
        this.studyEnvironmentDao = studyEnvironmentDao;
    }

    @Transactional
    public StudyEnvironment create(StudyEnvironment studyEnv) {
        return studyEnvironmentDao.create(studyEnv);
    }

    @Transactional
    public StudyEnvironment create(StudyEnvironment studyEnv, CascadeTree cascade) {
        return studyEnvironmentDao.create(studyEnv);
    }
}
