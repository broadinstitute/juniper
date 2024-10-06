package bio.terra.pearl.core.dao.study;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.dao.notification.TriggerDao;
import bio.terra.pearl.core.dao.survey.SurveyDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.notification.Trigger;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.survey.SurveyService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class StudyEnvironmentDao extends BaseMutableJdbiDao<StudyEnvironment> {
    private StudyEnvironmentConfigDao studyEnvironmentConfigDao;
    private StudyEnvironmentSurveyDao studyEnvironmentSurveyDao;
    private TriggerDao triggerDao;
    private SurveyService surveyService;
    private SurveyDao surveyDao;
    public StudyEnvironmentDao(Jdbi jdbi, StudyEnvironmentConfigDao studyEnvironmentConfigDao,
                               StudyEnvironmentSurveyDao studyEnvironmentSurveyDao,
                               TriggerDao triggerDao, SurveyService surveyService,
                               SurveyDao surveyDao) {
        super(jdbi);
        this.studyEnvironmentConfigDao = studyEnvironmentConfigDao;
        this.studyEnvironmentSurveyDao = studyEnvironmentSurveyDao;
        this.triggerDao = triggerDao;
        this.surveyService = surveyService;
        this.surveyDao = surveyDao;
    }

    @Override
    public Class<StudyEnvironment> getClazz() {
        return StudyEnvironment.class;
    }

    public List<StudyEnvironment> findByStudy(UUID studyId) {
        return findAllByProperty("study_id", studyId);
    }

    public Optional<StudyEnvironment> findByStudy(String shortcode, EnvironmentName environmentName) {
        return jdbi.withHandle(handle ->
                handle.createQuery("select " + prefixedGetQueryColumns("a") + " from " + tableName
                        + " a join study on study_id = study.id"
                        + " where study.shortcode = :shortcode and environment_name = :environmentName")
                        .bind("shortcode", shortcode)
                        .bind("environmentName", environmentName)
                        .mapTo(clazz)
                        .findOne()
        );
    }

    public List<StudyEnvironment> findAllByPortalAndEnvironment(UUID portalId, EnvironmentName environmentName) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                                 select %s from %s a
                                 join study on study_id = study.id
                                 join portal_study on study.id = portal_study.study_id
                                 where portal_study.portal_id = :portalId and environment_name = :environmentName
                                 """.formatted(prefixedGetQueryColumns("a"), tableName))
                        .bind("portalId", portalId)
                        .bind("environmentName", environmentName)
                        .mapTo(clazz)
                        .list()
        );
    }

    /**
     * returns all the studies associated with the given portal for the given environment
     * So, for example, if a portal has two studies, this might return the 'sandbox' environment for
     * both studies
     */
    public List<StudyEnvironment> findWithPreregContent(String portalShortcode, EnvironmentName envName) {
        List<String> primaryCols = getQueryColumns.stream().map(col -> "a." + col)
                .collect(Collectors.toList());
        List<StudyEnvironment> studyEnvs = jdbi.withHandle(handle ->
                handle.createQuery("select " + StringUtils.join(primaryCols, ", ") + " from " + tableName
                                + " a join portal_study on a.study_id = portal_study.study_id "
                                + " join portal on portal_study.portal_id = portal.id"
                                + " where portal.shortcode = :portalShortcode and a.environment_name = :environmentName")
                        .bind("portalShortcode", portalShortcode)
                        .bind("environmentName", envName)
                        .mapTo(clazz)
                        .list()
        );
        for (StudyEnvironment studyEnv : studyEnvs) {
            studyEnv.setStudyEnvironmentConfig(studyEnvironmentConfigDao
                    .find(studyEnv.getStudyEnvironmentConfigId()).get());
            if (studyEnv.getPreEnrollSurveyId() != null) {
                studyEnv.setPreEnrollSurvey(surveyDao.find(studyEnv.getPreEnrollSurveyId()).get());
            }
        };
        return studyEnvs;
    }

    /** populates the studyEnv object in-place with all the content -- consents, surveys, etc... */
    public StudyEnvironment attachAllContent(StudyEnvironment studyEnv) {
        UUID studyEnvId = studyEnv.getId();
        studyEnv.setStudyEnvironmentConfig(studyEnvironmentConfigDao.find(studyEnv.getStudyEnvironmentConfigId()).get());
        studyEnv.setConfiguredSurveys(studyEnvironmentSurveyDao.findAllWithSurvey(studyEnvId, true));
        if (studyEnv.getPreEnrollSurveyId() != null) {
            studyEnv.setPreEnrollSurvey(surveyService.find(studyEnv.getPreEnrollSurveyId()).get());
        }
        List<Trigger> triggers = triggerDao.findByStudyEnvironmentId(studyEnvId, true);
        triggerDao.attachTemplates(triggers);
        studyEnv.setTriggers(triggers);
        return studyEnv;
    }

    public void deleteByStudyId(UUID studyId) {
        deleteByProperty("study_id", studyId);
    }
}
