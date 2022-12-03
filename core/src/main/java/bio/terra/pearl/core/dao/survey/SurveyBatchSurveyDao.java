package bio.terra.pearl.core.dao.survey;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.survey.SurveyBatchSurvey;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class SurveyBatchSurveyDao extends BaseJdbiDao<SurveyBatchSurvey> {

    public SurveyBatchSurveyDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<SurveyBatchSurvey> getClazz() {
        return SurveyBatchSurvey.class;
    }

    public List<SurveyBatchSurvey> findByBatchId(UUID batchId) {
        return findAllByProperty("survey_batch_id", batchId);
    }

    public void deleteByBatchId(UUID batchId) {
        deleteByUuidProperty("survey_batch_id", batchId);
    }
}
