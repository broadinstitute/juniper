package bio.terra.pearl.api.admin.service.notifications;

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

  @Scheduled(
      fixedDelay = 10 * 60 * 1000,
      initialDelay = 5 * 1000) // wait 10mins between executions, start after 5 seconds
  @SchedulerLock(
      name = "EnrolleeReminderService.sendTaskReminders",
      lockAtMostFor = "500s",
      lockAtLeastFor = "10s")
  public void sendReminderEmails() {
    logger.info("Beginning enrollee reminder processing");
    enrolleeReminderService.sendTaskReminders();
    logger.info("Enrollee reminder processing complete");
  }
}
