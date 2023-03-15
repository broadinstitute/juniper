package bio.terra.pearl.api.admin.service;

import bio.terra.pearl.core.service.notification.EnrolleeReminderService;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ScheduledEnrolleeReminderService {
  private static final Logger logger = LoggerFactory.getLogger(EnrolleeReminderService.class);
  private EnrolleeReminderService enrolleeReminderService;

  public ScheduledEnrolleeReminderService(EnrolleeReminderService enrolleeReminderService) {
    this.enrolleeReminderService = enrolleeReminderService;
  }

  @Scheduled(fixedDelay = 60000, initialDelay = 5000) // wait 15mins between executions
  @SchedulerLock(
      name = "EnrolleeReminderService.sendTaskReminders",
      lockAtMostFor = "15s",
      lockAtLeastFor = "15s")
  public void sendReminderEmails() {
    logger.info("Beginning enrollee reminder processing");
    enrolleeReminderService.sendTaskReminders();
    logger.info("Enrollee reminder processing complete");
  }
}
