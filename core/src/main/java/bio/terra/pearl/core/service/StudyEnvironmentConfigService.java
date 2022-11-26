package bio.terra.pearl.core.service;

import bio.terra.pearl.core.dao.StudyEnvironmentConfigDao;
import bio.terra.pearl.core.model.StudyEnvironmentConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudyEnvironmentConfigService {
    @Autowired
    private StudyEnvironmentConfigDao studyEnvironmentConfigDao;

    @Transactional
    public StudyEnvironmentConfig create(StudyEnvironmentConfig config) {
        return studyEnvironmentConfigDao.create(config);
    }
}
