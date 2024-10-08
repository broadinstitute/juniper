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

    public Optional<SurveyQuestionDefinition> findByStableId(UUID portalId, String surveyStableId, String questionStableId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("SELECT survey_question_definition.* FROM survey_question_definition" +
                                " INNER JOIN survey ON survey_question_definition.survey_id = survey.id " +
                                " WHERE survey_stable_id = :surveyStableId AND question_stable_id = :questionStableId" +
                                " AND survey.portal_id = :portalId")
                        .bind("portalId", portalId)
                        .bind("surveyStableId", surveyStableId)
                        .bind("questionStableId", questionStableId)
                        .mapTo(clazz)
                        .findOne()
        );
    }
}
