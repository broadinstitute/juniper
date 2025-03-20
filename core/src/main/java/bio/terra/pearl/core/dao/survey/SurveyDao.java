package bio.terra.pearl.core.dao.survey;

import bio.terra.pearl.core.dao.BaseVersionedJdbiDao;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.survey.SurveyType;
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

    public Optional<Survey> findActiveByStudyEnvironmentIdAndStableIdNoContent(UUID studyEnvId, String stableId, Integer version) {
        return jdbi.withHandle(
                handle -> handle.createQuery("""
                                                            SELECT s.* FROM survey s
                                                                INNER JOIN study_environment_survey ses ON ses.survey_id = s.id
                                                                WHERE ses.study_environment_id = :studyEnvironmentId
                                                                AND s.stable_id = :stableId
                                                                AND s.version = :version
                                                                AND ses.active = true
                                """)
                        .bind("studyEnvironmentId", studyEnvId)
                        .bind("stableId", stableId)
                        .bind("version", version)
                        .mapTo(clazz)
                        .findOne()
        );
    }

    public Optional<Survey> findLatestActiveByStudyEnvironmentIdAndStableIdNoContent(UUID studyEnvId, String stableId) {
        return jdbi.withHandle(
                handle -> handle.createQuery("""
                                                            SELECT s.* FROM survey s
                                                                INNER JOIN study_environment_survey ses ON ses.survey_id = s.id
                                                                WHERE ses.study_environment_id = :studyEnvironmentId
                                                                AND s.stable_id = :stableId
                                                                AND ses.active = true
                                                                ORDER BY s.version DESC
                                                                LIMIT 1
                                """)
                        .bind("studyEnvironmentId", studyEnvId)
                        .bind("stableId", stableId)
                        .mapTo(clazz)
                        .findOne()
        );
    }

    @Override
    protected Class<Survey> getClazz() {
        return Survey.class;
    }

    public List<Survey> findActiveSurveysByPortalIdNoPreEnrolls(UUID portalId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                                select s.* from survey s
                                inner join study_environment_survey ses on s.id = ses.survey_id and ses.active = true
                                where s.portal_id = :portalId
                                """)
                        .bind("portalId", portalId)
                        .mapTo(clazz)
                        .list()
        );
    }

    public List<Survey> findActivePreEnrolleeSurveysByPortalId(UUID portalId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                                select s.* from survey s
                                inner join portal on s.portal_id = portal.id
                                inner join portal_study ps on portal.id = ps.portal_id
                                inner join study_environment se on ps.study_id = se.study_id
                                where s.portal_id = :portalId and s.id = se.pre_enroll_survey_id
                                """)
                        .bind("portalId", portalId)
                        .mapTo(clazz)
                        .list()
        );
    }

    public List<Survey> findActiveInStudyEnvWithType(UUID studyEnvId, SurveyType type) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                                select s.* from survey s
                                inner join study_environment_survey ses on s.id = ses.survey_id and ses.active = true
                                where ses.study_environment_id = :studyEnvId and s.survey_type = :type
                                """)
                        .bind("studyEnvId", studyEnvId)
                        .bind("type", type)
                        .mapTo(clazz)
                        .list()
        );
    }
}
