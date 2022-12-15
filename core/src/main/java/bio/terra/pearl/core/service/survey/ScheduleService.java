package bio.terra.pearl.core.service.survey;

import bio.terra.pearl.core.dao.survey.ScheduleDao;
import bio.terra.pearl.core.model.survey.Schedule;
import bio.terra.pearl.core.service.CrudService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ScheduleService extends CrudService<Schedule, ScheduleDao> {
    public ScheduleService(ScheduleDao scheduleDao) {
        super(scheduleDao);
    }

    @Transactional
    public void deleteByBatchId(UUID batchId) {
        dao.deleteByBatchId(batchId);
    }
}
