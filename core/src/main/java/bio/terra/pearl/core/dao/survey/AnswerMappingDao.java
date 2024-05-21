package bio.terra.pearl.core.dao.survey;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.survey.AnswerMapping;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import bio.terra.pearl.core.model.survey.AnswerMappingTargetType;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class AnswerMappingDao extends BaseJdbiDao<AnswerMapping> {
    public AnswerMappingDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<AnswerMapping> getClazz() {
        return AnswerMapping.class;
    }

    public List<AnswerMapping> findBySurveyId(UUID surveyId) {
        return findAllByProperty("survey_id", surveyId);
    }

    public void deleteBySurveyId(UUID surveyId) {
        deleteByProperty("survey_id", surveyId);
    }

    public Optional<AnswerMapping> findByTargetField(UUID surveyId, AnswerMappingTargetType targetType, String targetFieldName) {
            return jdbi.withHandle(handle ->
                    handle.createQuery("select * from " + tableName + " where survey_id = :surveyId"
                                    + " and target_type = :targetType and target_field = :fieldName;")
                            .bind("surveyId", surveyId)
                            .bind("targetType", targetType)
                            .bind("fieldName", targetFieldName)
                            .mapTo(clazz)
                            .findOne()
            );
        }

}
