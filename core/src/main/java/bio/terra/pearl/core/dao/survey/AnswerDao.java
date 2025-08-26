package bio.terra.pearl.core.dao.survey;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.survey.Answer;
import bio.terra.pearl.core.model.survey.AnswerFormat;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class AnswerDao extends BaseMutableJdbiDao<Answer> {
    public AnswerDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<Answer> getClazz() {
        return Answer.class;
    }

    public List<Answer> findByResponse(UUID surveyResponseId) {
        return findAllByProperty("survey_response_id", surveyResponseId);
    }

    public void deleteByResponseId(UUID responseId) {
        deleteByProperty("survey_response_id", responseId);
    }

    public Optional<Answer> findByResponseQuestion(UUID surveyResponseId, String questionStableId) {
        return findByTwoProperties("survey_response_id", surveyResponseId,
                "question_stable_id", questionStableId);
    }

    public List<Answer> findByResponseAndQuestions(UUID surveyResponseId, List<String> questionStableIds) {
        if (questionStableIds.isEmpty()) {
            // short circuit this case because bindList errors if list is empty
            return new ArrayList<>();
        }
        return jdbi.withHandle(handle ->
                handle.createQuery("select * from " + tableName
                                   + " where question_stable_id IN (<questionStableIds>) and survey_response_id = :surveyResponseId")
                        .bindList("questionStableIds", questionStableIds)
                        .bind("surveyResponseId", surveyResponseId)
                        .mapTo(clazz)
                        .list()
        );

    }

    public List<Answer> findByEnrollee(UUID enrolleeId) {
        return findAllByProperty("enrollee_id", enrolleeId);
    }

    public Map<UUID, List<Answer>> findByEnrolleeIds(Collection<UUID> enrolleeIds) {
        return findAllByPropertyCollection("enrollee_id", enrolleeIds)
                .stream().collect(Collectors.groupingBy(Answer::getEnrolleeId, Collectors.toList()));
    }

    public List<Answer> findByEnrolleeAndSurvey(UUID enrolleeId, String surveyStableId) {
        return findAllByTwoProperties("enrollee_id", enrolleeId, "survey_stable_id", surveyStableId);
    }

    public List<Answer> findByEnrolleeId(UUID enrolleeId) {
        return findAllByProperty("enrollee_id", enrolleeId);
    }

    public List<Answer> findByEnrolleeIdAndAnswerFormat(UUID enrolleeId, AnswerFormat answerFormat) {
        return findAllByTwoProperties("enrollee_id", enrolleeId, "format", answerFormat);
    }

    /**
     * Returns the most recent answer for a given enrollee, survey, and question.
     */
    public Optional<Answer> findForEnrolleeByQuestion(UUID enrolleeID, String surveyStableId, String questionStableId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("select * from " + tableName +
                                " where enrollee_id = :enrolleeId " +
                                "and survey_stable_id = :surveyStableId " +
                                "and question_stable_id = :questionStableId " +
                                "order by last_updated_at desc limit 1")
                        .bind("enrolleeId", enrolleeID)
                        .bind("surveyStableId", surveyStableId)
                        .bind("questionStableId", questionStableId)
                        .mapTo(clazz)
                        .findFirst()
        );
    }

    public Optional<Answer> findByProfileIdStudyAndQuestion(UUID profileId, String studyShortcode, String surveyStableId, String questionStableId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                                select a.* from %s a
                                inner join enrollee e on e.id = a.enrollee_id
                                inner join study_environment se on se.id = e.study_environment_id
                                inner join study s on s.id = se.study_id
                                where e.profile_id = :profileId
                                and s.shortcode = :studyShortcode
                                and a.survey_stable_id = :surveyStableId
                                and a.question_stable_id = :questionStableId
                                order by last_updated_at
                                desc limit 1
                                """.formatted(tableName))
                        .bind("profileId", profileId)
                        .bind("studyShortcode", studyShortcode)
                        .bind("surveyStableId", surveyStableId)
                        .bind("questionStableId", questionStableId)
                        .mapTo(clazz)
                        .findFirst()
        );
    }
}
