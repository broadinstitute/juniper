package bio.terra.pearl.core.service.study;

import bio.terra.pearl.core.dao.study.StudyEnvironmentConsentDao;
import bio.terra.pearl.core.dao.study.StudyEnvironmentDao;
import bio.terra.pearl.core.dao.study.StudyEnvironmentSurveyDao;
import bio.terra.pearl.core.dao.survey.PreEnrollmentResponseDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.consent.StudyEnvironmentConsent;
import bio.terra.pearl.core.model.notification.NotificationConfig;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironmentConfig;
import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.CrudService;
import bio.terra.pearl.core.service.datarepo.DataRepoJobService;
import bio.terra.pearl.core.service.datarepo.DatasetService;
import bio.terra.pearl.core.service.notification.NotificationConfigService;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.WithdrawnEnrolleeService;
import java.util.*;

import bio.terra.pearl.core.service.workflow.AdminTaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudyEnvironmentService extends CrudService<StudyEnvironment, StudyEnvironmentDao> {
    private StudyEnvironmentSurveyDao studyEnvironmentSurveyDao;
    private StudyEnvironmentConfigService studyEnvironmentConfigService;
    private EnrolleeService enrolleeService;
    private StudyEnvironmentConsentDao studyEnvironmentConsentDao;
    private PreEnrollmentResponseDao preEnrollmentResponseDao;
    private NotificationConfigService notificationConfigService;
    private DatasetService datasetService;
    private DataRepoJobService dataRepoJobService;
    private WithdrawnEnrolleeService withdrawnEnrolleeService;
    private AdminTaskService adminTaskService;


    public StudyEnvironmentService(StudyEnvironmentDao studyEnvironmentDao,
                                   StudyEnvironmentSurveyDao studyEnvironmentSurveyDao,
                                   StudyEnvironmentConfigService studyEnvironmentConfigService,
                                   EnrolleeService enrolleeService,
                                   StudyEnvironmentConsentDao studyEnvironmentConsentDao,
                                   PreEnrollmentResponseDao preEnrollmentResponseDao,
                                   NotificationConfigService notificationConfigService,
                                   DatasetService datasetService,
                                   DataRepoJobService dataRepoJobService,
                                   WithdrawnEnrolleeService withdrawnEnrolleeService,
                                   AdminTaskService adminTaskService) {
        super(studyEnvironmentDao);
        this.studyEnvironmentSurveyDao = studyEnvironmentSurveyDao;
        this.studyEnvironmentConfigService =  studyEnvironmentConfigService;
        this.enrolleeService = enrolleeService;
        this.studyEnvironmentConsentDao = studyEnvironmentConsentDao;
        this.preEnrollmentResponseDao = preEnrollmentResponseDao;
        this.notificationConfigService = notificationConfigService;
        this.datasetService = datasetService;
        this.dataRepoJobService = dataRepoJobService;
        this.withdrawnEnrolleeService = withdrawnEnrolleeService;
        this.adminTaskService = adminTaskService;
    }

    public List<StudyEnvironment> findByStudy(UUID studyId) {
        return dao.findByStudy(studyId);
    }

    public Optional<StudyEnvironment> findByStudy(String studyShortcode, EnvironmentName environmentName) {
        return dao.findByStudy(studyShortcode, environmentName);
    }

    public StudyEnvironment loadWithAllContent(StudyEnvironment studyEnvironment) {
        return dao.loadWithAllContent(studyEnvironment);
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
        for (NotificationConfig config : studyEnv.getNotificationConfigs()) {
            config.setStudyEnvironmentId(newEnv.getId());
            notificationConfigService.create(config);
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
        notificationConfigService.deleteByStudyEnvironmentId(studyEnvironmentId);
        preEnrollmentResponseDao.deleteByStudyEnvironmentId(studyEnvironmentId);
        dataRepoJobService.deleteByStudyEnvironmentId(studyEnvironmentId);
        datasetService.deleteByStudyEnvironmentId(studyEnvironmentId);
        withdrawnEnrolleeService.deleteByStudyEnvironmentId(studyEnvironmentId);
        adminTaskService.deleteByStudyEnvironmentId(studyEnvironmentId, null);
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
