package bio.terra.pearl.core.service.study;

import bio.terra.pearl.core.dao.study.StudyEnvironmentConsentDao;
import bio.terra.pearl.core.dao.study.StudyEnvironmentDao;
import bio.terra.pearl.core.dao.study.StudyEnvironmentSurveyDao;
import bio.terra.pearl.core.dao.survey.PreregistrationResponseDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.consent.StudyEnvironmentConsent;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironmentConfig;
import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.CrudService;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudyEnvironmentService extends CrudService<StudyEnvironment, StudyEnvironmentDao> {
    private StudyEnvironmentSurveyDao studyEnvironmentSurveyDao;
    private StudyEnvironmentConfigService studyEnvironmentConfigService;
    private EnrolleeService enrolleeService;
    private StudyEnvironmentConsentDao studyEnvironmentConsentDao;
    private PreregistrationResponseDao preregistrationResponseDao;


    public StudyEnvironmentService(StudyEnvironmentDao studyEnvironmentDao,
                                   StudyEnvironmentSurveyDao studyEnvironmentSurveyDao,
                                   StudyEnvironmentConfigService studyEnvironmentConfigService,
                                   EnrolleeService enrolleeService,
                                   StudyEnvironmentConsentDao studyEnvironmentConsentDao,
                                   PreregistrationResponseDao preregistrationResponseDao) {
        super(studyEnvironmentDao);
        this.studyEnvironmentSurveyDao = studyEnvironmentSurveyDao;
        this.studyEnvironmentConfigService =  studyEnvironmentConfigService;
        this.enrolleeService = enrolleeService;
        this.studyEnvironmentConsentDao = studyEnvironmentConsentDao;
        this.preregistrationResponseDao = preregistrationResponseDao;
    }

    public Set<StudyEnvironment> findByStudy(UUID studyId) {
        return new HashSet<>(dao.findByStudy(studyId));
    }

    public Optional<StudyEnvironment> findByStudy(String studyShortcode, EnvironmentName environmentName) {
        return dao.findByStudy(studyShortcode, environmentName);
    }

    public StudyEnvironment update(StudyEnvironment studyEnvironment) {
        return dao.update(studyEnvironment);
    }

    @Transactional
    @Override
    public StudyEnvironment create(StudyEnvironment studyEnv) {
        StudyEnvironmentConfig envConfig = studyEnv.getStudyEnvironmentConfig();
        if (studyEnv.getStudyEnvironmentConfig() != null) {
            envConfig = studyEnvironmentConfigService.create(envConfig);
            studyEnv.setStudyEnvironmentConfigId(envConfig.getId());
        }
        StudyEnvironment newEnv = dao.create(studyEnv);
        for (StudyEnvironmentSurvey studyEnvironmentSurvey : studyEnv.getConfiguredSurveys()) {
            studyEnvironmentSurvey.setStudyEnvironmentId(newEnv.getId());
            studyEnvironmentSurveyDao.create(studyEnvironmentSurvey);
        }
        for (StudyEnvironmentConsent studyEnvironmentConsent : studyEnv.getConfiguredConsents()) {
            studyEnvironmentConsent.setStudyEnvironmentId(newEnv.getId());
            studyEnvironmentConsentDao.create(studyEnvironmentConsent);
        }
        newEnv.setStudyEnvironmentConfig(envConfig);
        return newEnv;
    }

    @Transactional
    @Override
    public void delete(UUID studyEnvironmentId, Set<CascadeProperty> cascade) {
        StudyEnvironment studyEnv = dao.find(studyEnvironmentId).get();
        enrolleeService.deleteByStudyEnvironmentId(studyEnv.getId(), cascade);
        studyEnvironmentSurveyDao.deleteByStudyEnvironmentId(studyEnvironmentId);
        studyEnvironmentConsentDao.deleteByStudyEnvironmentId(studyEnvironmentId);
        dao.delete(studyEnvironmentId);
        if (studyEnv.getStudyEnvironmentConfigId() != null) {
            studyEnvironmentConfigService.delete(studyEnv.getStudyEnvironmentConfigId());
        }
    }

    @Transactional
    public void deleteByStudyId(UUID studyId, Set<CascadeProperty> cascade) {
        List<StudyEnvironment> studyEnvironments = dao.findByStudy(studyId);
        studyEnvironments.forEach(studyEnv -> {
            delete(studyEnv.getId(), cascade);
        });
    }

    public enum AllowedCascades implements CascadeProperty {
        ENVIRONMENT_CONFIG,
        ENROLLEE;
    }
}
