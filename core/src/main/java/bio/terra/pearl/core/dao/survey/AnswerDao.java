package bio.terra.pearl.core.dao.survey;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.survey.Answer;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    public List<Answer> findByEnrolleeAndSurvey(UUID enrolleeId, String surveyStableId) {
        return findAllByTwoProperties("enrollee_id", enrolleeId, "survey_stable_id", surveyStableId);
    }

    public List<Answer> findByEnrolleeId(UUID enrolleeId) {
        return findAllByProperty("enrollee_id", enrolleeId);
    }

    /**
     * Returns the most recent answer for a given enrollee, survey, and question.
     */
    public Answer findForEnrolleeByQuestion(UUID enrolleeID, String surveyStableId, String questionStableId) {
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
                        .orElse(null)
        );
    }
}
