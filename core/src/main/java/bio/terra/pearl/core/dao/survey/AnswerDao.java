package bio.terra.pearl.core.dao.survey;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.survey.Answer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

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

    public List<String> findBySurveyStableId(String surveyStableId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("select distinct question_stable_id from " + tableName
                                + " where survey_stable_id = :surveyStableId")
                        .bind("surveyStableId", surveyStableId)
                        .mapTo(String.class)
                        .list()
        );
    }

    public List<Answer> findByQuestionStableId(String questionStableId) {
        return findAllByProperty("question_stable_id", questionStableId);
    }

    public List<Answer> findByEnrolleeAndSurvey(UUID enrolleeId, String surveyStableId) {
        return findAllByTwoProperties("enrollee_id", enrolleeId, "survey_stable_id", surveyStableId);
    }

    public List<Answer> findByEnrolleeAndQuestionStableId(UUID enrolleeId, String questionStableId) {
        return findAllByTwoProperties("enrollee_id", enrolleeId, "question_stable_id", questionStableId);
    }

    public List<Answer> findByEnrolleeId(UUID enrolleeId) {
        return findAllByProperty("enrollee_id", enrolleeId);
    }
}
