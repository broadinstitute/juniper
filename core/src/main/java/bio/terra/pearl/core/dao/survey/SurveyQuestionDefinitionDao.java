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


    public Optional<SurveyQuestionDefinition> findByStableIds(String portalShortcode, String surveyStableId, String questionStableId) {
        return jdbi.withHandle(handle -> handle.createQuery(
                        """
                                SELECT sqd.* FROM survey_question_definition sqd
                                INNER JOIN survey s ON s.id = sqd.survey_id
                                INNER JOIN study_environment_survey ses ON s.id = ses.survey_id
                                INNER JOIN portal p ON p.id = s.portal_id
                                WHERE p.shortcode = :portalShortcode
                                AND s.stable_id = :surveyStableId
                                AND sqd.question_stable_id = :questionStableId
                                """)
                .bind("portalShortcode", portalShortcode)
                .bind("surveyStableId", surveyStableId)
                .bind("questionStableId", questionStableId)
                .mapToBean(SurveyQuestionDefinition.class)
                .findFirst());
    }
}
