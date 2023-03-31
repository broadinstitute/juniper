package bio.terra.pearl.core.dao.survey;

import bio.terra.pearl.core.dao.BaseJdbiDao;

import bio.terra.pearl.core.model.survey.SurveyQuestionDefinition;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

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

    public void deleteBySurveyId(UUID surveyId) {
        deleteByProperty("survey_id", surveyId);
    }

}
