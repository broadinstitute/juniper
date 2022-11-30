package bio.terra.pearl.core.service.study;

import bio.terra.pearl.core.dao.study.StudyEnvironmentConfigDao;
import bio.terra.pearl.core.model.study.StudyEnvironmentConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class StudyEnvironmentConfigService {
    @Autowired
    private StudyEnvironmentConfigDao studyEnvironmentConfigDao;

    @Transactional
    public StudyEnvironmentConfig create(StudyEnvironmentConfig config) {
        return studyEnvironmentConfigDao.create(config);
    }

    @Transactional
    public void deleteByStudyEnvironmentId(UUID studyEnvId) {
        studyEnvironmentConfigDao.deleteByStudyEnvironmentId(studyEnvId);
    }

    @Transactional
    public void delete(UUID configId) {
        studyEnvironmentConfigDao.delete(configId);
    }
}
