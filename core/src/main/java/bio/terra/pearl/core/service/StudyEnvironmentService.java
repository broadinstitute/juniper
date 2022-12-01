package bio.terra.pearl.core.service;

import bio.terra.pearl.core.dao.StudyEnvironmentDao;
import bio.terra.pearl.core.model.StudyEnvironment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Component
public class StudyEnvironmentService {
    private StudyEnvironmentDao studyEnvironmentDao;

    public StudyEnvironmentService(StudyEnvironmentDao studyEnvironmentDao) {
        this.studyEnvironmentDao = studyEnvironmentDao;
    }

    public Set<StudyEnvironment> findByStudy(UUID studyId) {
        return new HashSet<>(studyEnvironmentDao.findByStudy(studyId));
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
