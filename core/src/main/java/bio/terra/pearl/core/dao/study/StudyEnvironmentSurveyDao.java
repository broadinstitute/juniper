package bio.terra.pearl.core.dao.study;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.dao.survey.SurveyDao;
import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import bio.terra.pearl.core.model.survey.Survey;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Query;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class StudyEnvironmentSurveyDao extends BaseMutableJdbiDao<StudyEnvironmentSurvey> {
    private SurveyDao surveyDao;
    private StudyEnvironmentDao studyEnvironmentDao;
    public StudyEnvironmentSurveyDao(Jdbi jdbi, SurveyDao surveyDao, @Lazy StudyEnvironmentDao studyEnvironmentDao) {
        super(jdbi);
        this.surveyDao = surveyDao;
        this.studyEnvironmentDao = studyEnvironmentDao;
    }

    @Override
    protected Class<StudyEnvironmentSurvey> getClazz() {
        return StudyEnvironmentSurvey.class;
    }

    public void deleteByStudyEnvironmentId(UUID studyEnvId) {
        deleteByProperty("study_environment_id", studyEnvId);
    }

    public List<StudyEnvironmentSurvey> findBySurveyId(UUID surveyId) {
        return findAllByProperty("survey_id", surveyId);
    }

    public void deleteBySurveyId(UUID surveyId) {
        deleteByProperty("survey_id", surveyId);
    }

    /** finds by a surveyId and studyEnvironment, limited to active surveys */
    public Optional<StudyEnvironmentSurvey> findActiveBySurvey(UUID studyEnvId, UUID surveyId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                                select * from %s
                                    where study_environment_id = :studyEnvId
                                    and survey_id = :surveyId
                                    and a.active = true;
                                """.formatted(prefixedGetQueryColumns("a"), tableName))
                        .bind("surveyId", surveyId)
                        .bind("studyEnvId", studyEnvId)
                        .mapTo(clazz)
                        .findOne()
        );
    }

    public List<StudyEnvironmentSurvey> findActiveBySurvey(UUID studyEnvId, String surveyStableId) {
        return findAll(List.of(studyEnvId), surveyStableId, true);
    }

    public List<StudyEnvironmentSurvey> findAll(List<UUID> studyEnvIds, String surveyStableId, Boolean active) {
        return jdbi.withHandle(handle -> {
                Query query = handle.createQuery("""
                                select %s from %s a
                                    join survey on survey.id = a.survey_id
                                    where a.study_environment_id IN (<studyEnvIds>)
                                    %s
                                    %s
                                    order by survey.stable_id asc, survey_order asc;
                                """.formatted(
                                        prefixedGetQueryColumns("a"),
                                        tableName,
                                        surveyStableId != null ? " and survey.stable_id = :stableId" : "",
                                        active != null ? " and a.active = :active" : ""))
                        .bind("stableId", surveyStableId)
                        .bindList("studyEnvIds", studyEnvIds);
                if (active != null) {
                    query = query.bind("active", active);
                }
                if (surveyStableId != null) {
                    query = query.bind("stableId", surveyStableId);
                }
                return query.mapTo(clazz)
                        .list();
        });
    }

    /** gets all the study environment surveys and attaches the relevant survey objects in a batch */
    public List<StudyEnvironmentSurvey> findAllWithSurvey(UUID studyEnvId, Boolean active) {
        List<StudyEnvironmentSurvey> studyEnvSurvs = findAll(List.of(studyEnvId), null, active);
        attachSurveys(studyEnvSurvs, ATTACH_SURVEY.WITH_CONTENT);
        return studyEnvSurvs;
    }

    public List<StudyEnvironmentSurvey> findAllWithSurveyNoContent(List<UUID> studyEnvironmentIds, String stableId, Boolean active) {
        List<StudyEnvironmentSurvey> studyEnvSurveys = findAll(studyEnvironmentIds, stableId, active);
        attachSurveys(studyEnvSurveys, ATTACH_SURVEY.WITHOUT_CONTENT);
        return studyEnvSurveys;
    }

    protected void attachSurveys(List<StudyEnvironmentSurvey> studyEnvSurveys, ATTACH_SURVEY attach) {
        List<UUID> surveyIds = studyEnvSurveys.stream().map(ses -> ses.getSurveyId()).collect(Collectors.toList());
        List<Survey> surveys = attach.equals(ATTACH_SURVEY.WITH_CONTENT) ? surveyDao.findAll(surveyIds) : surveyDao.findAllNoContent(surveyIds);
        for (StudyEnvironmentSurvey ses : studyEnvSurveys) {
            ses.setSurvey(surveys.stream().filter(survey -> survey.getId().equals(ses.getSurveyId()))
                    .findFirst().orElseThrow());
        }
    }

    /** we only want one version of a given survey to be active in an environment at a time */
    public boolean isSurveyActiveInEnv(UUID surveyId, UUID studyEnvId, UUID excludeId) {
        String exclusionQuery = excludeId != null ? " and a.id != :excludeId" : "";
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                                select count(*) from %s a
                                    join survey on survey.id = a.survey_id
                                    where a.study_environment_id = :studyEnvId
                                    and stable_id = (select stable_id from survey where id = :surveyId)                                 
                                    and a.active = true
                                    %s;
                                """.formatted(tableName, exclusionQuery))
                        .bind("studyEnvId", studyEnvId)
                        .bind("surveyId", surveyId)
                        .bind("excludeId", excludeId)
                        .mapTo(Integer.class)
                        .one()) > 0;
    }

    protected enum ATTACH_SURVEY {
        WITH_CONTENT, // include the content json of the survey
        WITHOUT_CONTENT  // exclude the content from the retrieval
    }
}
