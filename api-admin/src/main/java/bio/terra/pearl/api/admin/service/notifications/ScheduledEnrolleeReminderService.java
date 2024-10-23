package bio.terra.pearl.api.admin.service.notifications;

import bio.terra.pearl.core.service.notification.EnrolleeReminderService;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ScheduledEnrolleeReminderService {
  private final EnrolleeReminderService enrolleeReminderService;

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
    log.info("Beginning enrollee reminder processing");
    enrolleeReminderService.sendTaskReminders();
    log.info("Enrollee reminder processing complete");
  }
}
