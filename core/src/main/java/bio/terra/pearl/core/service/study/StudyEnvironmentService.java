package bio.terra.pearl.core.service.study;

import bio.terra.pearl.core.dao.study.StudyEnvironmentDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironmentConfig;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class StudyEnvironmentService {
    private StudyEnvironmentDao studyEnvironmentDao;
    private StudyEnvironmentConfigService studyEnvironmentConfigService;
    private EnrolleeService enrolleeService;


    public StudyEnvironmentService(StudyEnvironmentDao studyEnvironmentDao,
                                   StudyEnvironmentConfigService studyEnvironmentConfigService,
                                   EnrolleeService enrolleeService) {
        this.studyEnvironmentDao = studyEnvironmentDao;
        this.studyEnvironmentConfigService =  studyEnvironmentConfigService;
        this.enrolleeService = enrolleeService;
    }

    public Set<StudyEnvironment> findByStudy(UUID studyId) {
        return new HashSet<>(studyEnvironmentDao.findByStudy(studyId));
    }

    public Optional<StudyEnvironment> findByStudy(String studyShortcode, EnvironmentName environmentName) {
        return studyEnvironmentDao.findByStudy(studyShortcode, environmentName);
    }

    @Transactional
    public StudyEnvironment create(StudyEnvironment studyEnv) {
        StudyEnvironmentConfig envConfig = studyEnv.getStudyEnvironmentConfig();
        if (studyEnv.getStudyEnvironmentConfig() != null) {
            envConfig = studyEnvironmentConfigService.create(envConfig);
            studyEnv.setStudyEnvironmentConfigId(envConfig.getId());
        }
        StudyEnvironment newEnv = studyEnvironmentDao.create(studyEnv);
        newEnv.setStudyEnvironmentConfig(envConfig);
        return newEnv;
    }

    @Transactional
    public void delete(UUID studyEnvironmentId) {
        StudyEnvironment studyEnv = studyEnvironmentDao.find(studyEnvironmentId).get();
        studyEnvironmentDao.delete(studyEnvironmentId);
        if (studyEnv.getStudyEnvironmentConfigId() != null) {
            studyEnvironmentConfigService.delete(studyEnv.getStudyEnvironmentConfigId());
        }
    }

    public void deleteByStudyId(UUID studyId, Set<CascadeProperty> cascade) {
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
