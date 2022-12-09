package bio.terra.pearl.core.service.study;

import bio.terra.pearl.core.dao.study.StudyEnvironmentDao;
import bio.terra.pearl.core.dao.survey.StudyEnvrionmentSurveyDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironmentConfig;
import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import bio.terra.pearl.core.model.survey.SurveyBatch;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.survey.SurveyBatchService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class StudyEnvironmentService {
    private StudyEnvironmentDao studyEnvironmentDao;
    private StudyEnvrionmentSurveyDao studyEnvironmentSurveyDao;
    private StudyEnvironmentConfigService studyEnvironmentConfigService;
    private SurveyBatchService surveyBatchService;
    private EnrolleeService enrolleeService;


    public StudyEnvironmentService(StudyEnvironmentDao studyEnvironmentDao,
                                   StudyEnvrionmentSurveyDao studyEnvironmentSurveyDao,
                                   StudyEnvironmentConfigService studyEnvironmentConfigService,
                                   SurveyBatchService surveyBatchService, EnrolleeService enrolleeService) {
        this.studyEnvironmentDao = studyEnvironmentDao;
        this.studyEnvironmentSurveyDao = studyEnvironmentSurveyDao;
        this.studyEnvironmentConfigService =  studyEnvironmentConfigService;
        this.surveyBatchService = surveyBatchService;
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
        for (StudyEnvironmentSurvey studyEnvironmentSurvey : studyEnv.getStudyEnvironmentSurveys()) {
            studyEnvironmentSurvey.setStudyEnvironmentId(newEnv.getId());
            studyEnvironmentSurveyDao.create(studyEnvironmentSurvey);
        }
        for (SurveyBatch surveyBatch : studyEnv.getSurveyBatches()) {
            surveyBatch.setStudyEnvironmentId(newEnv.getId());
            surveyBatchService.create(surveyBatch);
        }
        newEnv.setStudyEnvironmentConfig(envConfig);
        return newEnv;
    }

    @Transactional
    public void delete(UUID studyEnvironmentId, Set<CascadeProperty> cascade) {
        StudyEnvironment studyEnv = studyEnvironmentDao.find(studyEnvironmentId).get();
        enrolleeService.deleteByStudyEnvironmentId(studyEnv.getId(), cascade);
        studyEnvironmentSurveyDao.deleteByStudyEnvironmentId(studyEnvironmentId);
        studyEnvironmentDao.delete(studyEnvironmentId);
        if (studyEnv.getStudyEnvironmentConfigId() != null) {
            studyEnvironmentConfigService.delete(studyEnv.getStudyEnvironmentConfigId());
        }
    }

    @Transactional
    public void deleteByStudyId(UUID studyId, Set<CascadeProperty> cascade) {
        List<StudyEnvironment> studyEnvironments = studyEnvironmentDao.findByStudy(studyId);
        studyEnvironments.forEach(studyEnv -> {
            delete(studyEnv.getId(), cascade);
        });
    }

    public enum AllowedCascades implements CascadeProperty {
        ENVIRONMENT_CONFIG,
        ENROLLEE;
    }
}
