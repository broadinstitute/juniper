package bio.terra.pearl.core.service;

import bio.terra.pearl.core.dao.StudyEnvironmentDao;
import bio.terra.pearl.core.model.StudyEnvironment;
import bio.terra.pearl.core.model.StudyEnvironmentConfig;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class StudyEnvironmentService {
    private StudyEnvironmentDao studyEnvironmentDao;
    private StudyEnvironmentConfigService environmentConfigService;
    private EnrolleeService enrolleeService;

    public StudyEnvironmentService(StudyEnvironmentDao studyEnvironmentDao,
                                   StudyEnvironmentConfigService environmentConfigService,
                                   EnrolleeService enrolleeService) {
        this.studyEnvironmentDao = studyEnvironmentDao;
        this.environmentConfigService =  environmentConfigService;
        this.enrolleeService = enrolleeService;
    }

    public Set<StudyEnvironment> findByStudy(UUID studyId) {
        return new HashSet<>(studyEnvironmentDao.findByStudy(studyId));
    }


    @Transactional
    public StudyEnvironment create(StudyEnvironment studyEnv) {
        StudyEnvironment newEnv = studyEnvironmentDao.create(studyEnv);
        if (studyEnv.getStudyEnvironmentConfig() != null) {
            StudyEnvironmentConfig envConfig = studyEnv.getStudyEnvironmentConfig();
            envConfig.setStudyEnvironmentId(newEnv.getId());
            newEnv.setStudyEnvironmentConfig(environmentConfigService.create(envConfig));
        }
        return newEnv;
    }

    @Transactional
    public void delete(UUID studyEnvironmentId) {
        environmentConfigService.deleteByStudyEnvironmentId(studyEnvironmentId);
        studyEnvironmentDao.delete(studyEnvironmentId);
    }

    public void deleteByStudyId(UUID studyId, CascadeTree cascade) {
        List<StudyEnvironment> studyEnvironments = studyEnvironmentDao.findByStudy(studyId);
        studyEnvironments.forEach(studyEnv -> {
            enrolleeService.deleteByStudyEnvironmentId(studyEnv.getId(), cascade);
            delete(studyEnv.getId());
        });
    }

    public enum AllowedCascades implements CascadeProperty {
        ENVIRONMENT_CONFIG,
        ENROLLEE;
    }
}
