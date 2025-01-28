package bio.terra.pearl.api.admin.service.scheduled;

import bio.terra.pearl.api.admin.service.system.CheckDisableScheduledTask;
import bio.terra.pearl.core.service.notification.EnrolleeLaunchService;
import bio.terra.pearl.core.service.notification.EnrolleeReminderService;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ScheduledEnrolleeReminderService {
  private final EnrolleeReminderService enrolleeReminderService;
  private final EnrolleeLaunchService enrolleeLaunchService;

  public ScheduledEnrolleeReminderService(
      EnrolleeReminderService enrolleeReminderService,
      EnrolleeLaunchService enrolleeLaunchService) {
    this.enrolleeReminderService = enrolleeReminderService;
    this.enrolleeLaunchService = enrolleeLaunchService;
  }

  @Scheduled(
      fixedDelay = 10 * 60 * 1000,
      initialDelay = 5 * 1000) // wait 10mins between executions, start after 5 seconds
  @SchedulerLock(
      name = "EnrolleeReminderService.sendTaskReminders",
      lockAtMostFor = "500s",
      lockAtLeastFor = "10s")
  @CheckDisableScheduledTask
  public void sendReminderEmails() {
    log.info("Beginning enrollee reminder processing");
    enrolleeReminderService.sendTaskReminders();
    enrolleeLaunchService.sendTaskLaunchEmails();
    log.info("Enrollee reminder processing complete");
  }
}
