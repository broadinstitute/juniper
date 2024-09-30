package bio.terra.pearl.core.dao.survey;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.survey.ReferencedQuestion;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class ReferencedQuestionDao extends BaseJdbiDao<ReferencedQuestion> {
    public ReferencedQuestionDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<ReferencedQuestion> getClazz() {
        return ReferencedQuestion.class;
    }

    public List<ReferencedQuestion> findBySurveyId(UUID surveyId) {
        return findAllByProperty("survey_id", surveyId);
    }


}
