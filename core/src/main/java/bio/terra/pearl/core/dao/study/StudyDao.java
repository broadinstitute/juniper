package bio.terra.pearl.core.dao.study;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironmentConfig;
import bio.terra.pearl.core.service.survey.SurveyService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class StudyDao  extends BaseJdbiDao<Study> {
    private StudyEnvironmentDao studyEnvironmentDao;
    private StudyEnvironmentConfigDao studyEnvironmentConfigDao;
    private StudyEnvironmentSurveyDao studyEnvironmentSurveyDao;
    private StudyEnvironmentConsentDao studyEnvironmentConsentDao;
    private SurveyService surveyService;

    @Override
    protected Class<Study> getClazz() {
        return Study.class;
    }

    public StudyDao(Jdbi jdbi, StudyEnvironmentDao studyEnvironmentDao,
                    StudyEnvironmentConfigDao studyEnvironmentConfigDao,
                    StudyEnvironmentSurveyDao studyEnvironmentSurveyDao,
                    StudyEnvironmentConsentDao studyEnvironmentConsentDao, SurveyService surveyService) {
        super(jdbi);
        this.studyEnvironmentDao = studyEnvironmentDao;
        this.studyEnvironmentConfigDao = studyEnvironmentConfigDao;
        this.studyEnvironmentSurveyDao = studyEnvironmentSurveyDao;
        this.studyEnvironmentConsentDao = studyEnvironmentConsentDao;
        this.surveyService = surveyService;
    }

    public Optional<Study> findOneByShortcode(String shortcode) {
        return findByProperty("shortcode", shortcode);
    }

    public Optional<Study> findOneFullLoad(UUID id) {
        Optional<Study> studyOpt = find(id);
        studyOpt.ifPresent(study -> {
            List<StudyEnvironment> studyEnvs = studyEnvironmentDao.findByStudy(id);
            List<UUID> configIds = studyEnvs.stream().map(env -> env.getStudyEnvironmentConfigId())
                    .collect(Collectors.toList());
            List<StudyEnvironmentConfig> configs = studyEnvironmentConfigDao.findAll(configIds);
            /**
             * Iterate through each environment and load the content.  This could be optimized further,
             * by batching queries across environments, but since it's
             * the admin UI that loads studies like this, speed isn't as important
             */
            for (StudyEnvironment studyEnv : studyEnvs) {
                study.getStudyEnvironments().add(studyEnv);
                studyEnv.setStudyEnvironmentConfig(configs.stream()
                        .filter(config -> config.getId().equals(studyEnv.getStudyEnvironmentConfigId()))
                        .findFirst().get());
                studyEnv.setConfiguredSurveys(studyEnvironmentSurveyDao.findAllByStudyEnvIdWithSurvey(studyEnv.getId()));
                if (studyEnv.getPreRegSurveyId() != null) {
                    studyEnv.setPreRegSurvey(surveyService.find(studyEnv.getPreRegSurveyId()).get());
                }
                studyEnv.setConfiguredConsents(studyEnvironmentConsentDao
                        .findAllByStudyEnvIdWithConsent(studyEnv.getId()));

            }
        });
        return studyOpt;
    }
}
