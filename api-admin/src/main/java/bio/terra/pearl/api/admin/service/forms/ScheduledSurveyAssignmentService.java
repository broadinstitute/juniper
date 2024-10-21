package bio.terra.pearl.api.admin.service.forms;

import bio.terra.pearl.core.service.survey.SurveyTaskDispatcher;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
public class ScheduledSurveyAssignmentService {
  private final SurveyTaskDispatcher surveyTaskDispatcher;

  public ScheduledSurveyAssignmentService(SurveyTaskDispatcher surveyTaskDispatcher) {
    this.surveyTaskDispatcher = surveyTaskDispatcher;
  }

  @Scheduled(
      fixedDelay = 60 * 60 * 1000,
      initialDelay = 5 * 1000) // wait an hour between executions, start after 5 seconds
  @SchedulerLock(
      name = "ScheduledSurveyAssignmentService.assignScheduledSurveys",
      lockAtMostFor = "500s",
      lockAtLeastFor = "10s")
  public void assignScheduledSurveys() {
    log.info("Scheduled survey processing beginning");
    surveyTaskDispatcher.assignScheduledSurveys();
    log.info("Scheduled survey processing complete");
  }
}
