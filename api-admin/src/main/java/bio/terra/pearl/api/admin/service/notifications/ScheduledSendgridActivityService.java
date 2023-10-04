package bio.terra.pearl.api.admin.service.notifications;

import java.util.concurrent.TimeUnit;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ScheduledSendgridActivityService {
  SendgridActivityService sendgridActivityService;

  public ScheduledSendgridActivityService(SendgridActivityService sendgridActivityService) {
    this.sendgridActivityService = sendgridActivityService;
  }

  @Scheduled(timeUnit = TimeUnit.MINUTES, fixedDelay = 30, initialDelay = 1)
  @SchedulerLock(
      name = "SendgridActivityService.saveSendgridActivity",
      lockAtLeastFor = "1m",
      lockAtMostFor = "15m")
  public void saveSendgridActivity() throws Exception {
    sendgridActivityService.saveSendgridActivity();
  }
}
