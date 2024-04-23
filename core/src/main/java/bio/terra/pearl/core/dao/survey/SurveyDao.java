package bio.terra.pearl.core.dao.survey;

import bio.terra.pearl.core.dao.BaseVersionedJdbiDao;
import bio.terra.pearl.core.model.survey.Survey;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class SurveyDao extends BaseVersionedJdbiDao<Survey> {
    private AnswerMappingDao answerMappingDao;
    private final String columnsWithNoContentString;
    public SurveyDao(Jdbi jdbi, AnswerMappingDao answerMappingDao) {
        super(jdbi);
        this.answerMappingDao = answerMappingDao;
        columnsWithNoContentString = getQueryColumns.stream().filter(column -> !column.equals("content")).collect(Collectors.joining(","));
    }

    public Optional<Survey> findByStableIdWithMappings(String stableId, int version, UUID portalId) {
        Optional<Survey> surveyOpt = findByStableId(stableId, version, portalId);
        surveyOpt.ifPresent(survey -> {
            survey.setAnswerMappings(answerMappingDao.findBySurveyId(survey.getId()));
        });
        return surveyOpt;
    }

    public Optional<Survey> findByStableIdAndPortalShortcodeWithMappings(String stableId, int version, String shortcode) {
        Optional<Survey> surveyOpt = findByStableIdAndPortalShortcode(stableId, version, shortcode);
        surveyOpt.ifPresent(survey -> {
            survey.setAnswerMappings(answerMappingDao.findBySurveyId(survey.getId()));
        });
        return surveyOpt;
    }

    public List<Survey> findByStableIdNoContent(String stableId) {
        List<Survey> surveys = jdbi.withHandle(handle ->
                handle.createQuery("select %s from survey where stable_id = :stableId;".formatted(columnsWithNoContentString))
                        .bind("stableId", stableId)
                        .mapTo(clazz)
                        .list()
        );
        return surveys;
    }

    public List<Survey> findByPortalId(UUID portalId) {
        return findAllByProperty("portal_id", portalId);
    }

    /** get all the surveys, but without the content populated */
    public List<Survey> findAllNoContent(List<UUID> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                        select %s 
                        from survey 
                        where id IN (<ids>);""".formatted(columnsWithNoContentString))
                        .bindList("ids", ids)
                        .mapTo(clazz)
                        .list()
        );
    }

    public List<Survey> findByStudyEnvironmentIdWithContent(UUID studyEnvironmentId) {
        return jdbi.withHandle(
                handle -> handle.createQuery("""
                                SELECT s.* FROM survey s
                                    INNER JOIN study_environment_survey ses ON ses.survey_id = s.id
                                    WHERE ses.study_environment_id = :studyEnvironmentId""")
                        .bind("studyEnvironmentId", studyEnvironmentId)
                        .mapTo(clazz)
                        .list()
        );
    }

    public Survey findPreEnrollForStudyEnv(UUID studyEnvId) {
        return jdbi.withHandle(
                handle -> handle.createQuery("""
                                SELECT s.* FROM survey s
                                    INNER JOIN study_environment_survey ses ON ses.survey_id = s.id
                                    INNER JOIN study_environment se ON se.id = ses.study_environment_id
                                    WHERE se.pre_enroll_survey_id = s.id
                                    AND ses.study_environment_id = :studyEnvironmentId
                                """)
                        .bind("studyEnvironmentId", studyEnvId)
                        .mapTo(clazz)
                        .first()
        );
    }

    @Override
    protected Class<Survey> getClazz() {
        return Survey.class;
    }
}
