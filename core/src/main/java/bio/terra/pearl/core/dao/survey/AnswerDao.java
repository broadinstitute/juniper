package bio.terra.pearl.core.dao.survey;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.survey.Answer;
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

    public Optional<Answer> findForQuestion(UUID surveyResponseId, String questionStableId) {
        return findByTwoProperties("survey_response_id", surveyResponseId,
                "question_stable_id", questionStableId);
    }

    public List<Answer> findByEnrolleeId(UUID enrolleeId) {
        return findAllByProperty("enrollee_id", enrolleeId);
    }
}
