package bio.terra.pearl.core.service;

import bio.terra.pearl.core.dao.StudyEnvironmentDao;
import bio.terra.pearl.core.model.EnvironmentConfig;
import bio.terra.pearl.core.model.StudyEnvironment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Component
public class StudyEnvironmentService {
    private StudyEnvironmentDao studyEnvironmentDao;
    private EnvironmentConfigService environmentConfigService;

    public StudyEnvironmentService(StudyEnvironmentDao studyEnvironmentDao,  EnvironmentConfigService environmentConfigService) {
        this.studyEnvironmentDao = studyEnvironmentDao;
        this.environmentConfigService =  environmentConfigService;
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
        StudyEnvironment newEnv = create(studyEnv);
        if (cascade.hasProperty(AllowedCascades.ENVIRONMENT_CONFIG) && studyEnv.getEnvironmentConfig() != null) {
            EnvironmentConfig envConfig = studyEnv.getEnvironmentConfig();
            envConfig.setStudyEnvironmentId(newEnv.getId());
            newEnv.setEnvironmentConfig(environmentConfigService.create(envConfig));
        }
        return newEnv;
    }

    public enum AllowedCascades implements CascadeProperty {
        ENVIRONMENT_CONFIG;
    }
}
