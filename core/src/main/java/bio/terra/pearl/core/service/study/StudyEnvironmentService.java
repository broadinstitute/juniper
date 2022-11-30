package bio.terra.pearl.core.service.study;

import bio.terra.pearl.core.dao.study.StudyEnvironmentDao;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironmentConfig;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.CascadeTree;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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


    @Transactional
    public StudyEnvironment create(StudyEnvironment studyEnv) {
        StudyEnvironment newEnv = studyEnvironmentDao.create(studyEnv);
        if (studyEnv.getStudyEnvironmentConfig() != null) {
            StudyEnvironmentConfig envConfig = studyEnv.getStudyEnvironmentConfig();
            newEnv.setStudyEnvironmentConfig(studyEnvironmentConfigService.create(envConfig));
            newEnv.setStudyEnvironmentConfigId(envConfig.getId());
        }
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
