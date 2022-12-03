package bio.terra.pearl.core.dao.survey;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.survey.Schedule;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ScheduleDao extends BaseJdbiDao<Schedule> {
    private SurveyBatchDao surveyBatchDao;
    public ScheduleDao(Jdbi jdbi, SurveyBatchDao surveyBatchDao) {
        super(jdbi);
        this.surveyBatchDao = surveyBatchDao;
    }

    @Override
    protected Class<Schedule> getClazz() {
        return Schedule.class;
    }

    public void deleteByBatchId(UUID batchId) {
        deleteByParentUuid("schedule_id", batchId, surveyBatchDao);
    }
}
