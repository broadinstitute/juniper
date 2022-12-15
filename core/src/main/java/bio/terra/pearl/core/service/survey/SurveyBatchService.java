package bio.terra.pearl.core.service.survey;

import bio.terra.pearl.core.dao.survey.SurveyBatchDao;
import bio.terra.pearl.core.dao.survey.SurveyBatchSurveyDao;
import bio.terra.pearl.core.model.survey.Schedule;
import bio.terra.pearl.core.model.survey.SurveyBatch;
import bio.terra.pearl.core.model.survey.SurveyBatchSurvey;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.CrudService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class SurveyBatchService extends CrudService<SurveyBatch, SurveyBatchDao> {
    private ScheduleService scheduleService;
    private SurveyBatchSurveyDao surveyBatchSurveyDao;

    public SurveyBatchService(ScheduleService scheduleService, SurveyBatchDao surveyBatchDao,
                              SurveyBatchSurveyDao surveyBatchSurveyDao) {
        super(surveyBatchDao);
        this.scheduleService = scheduleService;
        this.surveyBatchSurveyDao = surveyBatchSurveyDao;
    }

    @Transactional
    public SurveyBatch create(SurveyBatch surveyBatch) {
        Schedule schedule = null;
        if (surveyBatch.getSchedule() != null) {
            schedule = scheduleService.create(surveyBatch.getSchedule());
            surveyBatch.setScheduleId(schedule.getId());
        }

        SurveyBatch savedBatch = dao.create(surveyBatch);

        for (SurveyBatchSurvey sbSurvey : surveyBatch.getSurveyBatchSurveys()) {
            sbSurvey.setSurveyBatchId(savedBatch.getId());
            savedBatch.getSurveyBatchSurveys().add(surveyBatchSurveyDao.create(sbSurvey));
        }
        savedBatch.setSchedule(schedule);
        return savedBatch;
    }

    @Transactional
    public void delete(UUID surveyBatchId) {
        scheduleService.deleteByBatchId(surveyBatchId);
        surveyBatchSurveyDao.deleteByBatchId(surveyBatchId);
        dao.delete(surveyBatchId);
    }

    @Transactional
    public void deleteByStudyEnvironmentId(UUID studyEnvironmentId, Set<CascadeProperty> cascade) {
        List<SurveyBatch> surveyBatches = dao.findByStudyEnvironmentId(studyEnvironmentId);
        for (SurveyBatch surveyBatch : surveyBatches) {
            delete(surveyBatch.getId());
        }
    }
}
