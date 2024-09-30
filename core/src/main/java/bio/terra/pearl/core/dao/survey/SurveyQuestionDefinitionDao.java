package bio.terra.pearl.core.dao.survey;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.survey.SurveyQuestionDefinition;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class SurveyQuestionDefinitionDao extends BaseJdbiDao<SurveyQuestionDefinition> {
    public SurveyQuestionDefinitionDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<SurveyQuestionDefinition> getClazz() {
        return SurveyQuestionDefinition.class;
    }

    public List<SurveyQuestionDefinition> findAllBySurveyIds(List<UUID> surveyIds) {
        return findAllByPropertyCollection("survey_id", surveyIds);
    }

    public List<SurveyQuestionDefinition> findAllBySurveyId(UUID surveyId) {
        return findAllByProperty("survey_id", surveyId);
    }

    public void deleteBySurveyId(UUID surveyId) {
        deleteByProperty("survey_id", surveyId);
    }

    public Optional<SurveyQuestionDefinition> findByStableId(String surveyStableId, String questionStableId) {
        return findByTwoProperties("survey_stable_id", surveyStableId, "question_stable_id", questionStableId);
    }
}
