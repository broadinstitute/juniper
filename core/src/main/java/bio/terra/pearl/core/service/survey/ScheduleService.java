package bio.terra.pearl.core.service.survey;

import bio.terra.pearl.core.dao.survey.ScheduleDao;
import bio.terra.pearl.core.model.survey.Schedule;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ScheduleService {
    private ScheduleDao scheduleDao;

    public ScheduleService(ScheduleDao scheduleDao) {
        this.scheduleDao = scheduleDao;
    }

    @Transactional
    public Schedule create(Schedule schedule) {
        return scheduleDao.create(schedule);
    }

    @Transactional
    public void delete(UUID scheduleId) {
        scheduleDao.delete(scheduleId);
    }

    @Transactional
    public void deleteByBatchId(UUID batchId) {
        scheduleDao.deleteByBatchId(batchId);
    }
}
