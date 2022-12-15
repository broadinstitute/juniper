package bio.terra.pearl.core.dao.survey;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.survey.SurveyBatch;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class SurveyBatchDao extends BaseJdbiDao<SurveyBatch> {
    public SurveyBatchDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<SurveyBatch> getClazz() {
        return SurveyBatch.class;
    }

    public List<SurveyBatch> findByStudyEnvironmentId(UUID studyEnvironmentId) {
        return findAllByProperty("study_environment_id", studyEnvironmentId);
    }
}
