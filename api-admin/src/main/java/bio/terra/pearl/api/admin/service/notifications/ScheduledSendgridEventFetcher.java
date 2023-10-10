package bio.terra.pearl.api.admin.service.notifications;

import java.util.concurrent.TimeUnit;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ScheduledSendgridEventFetcher {
  SendgridEventService sendgridEventService;

  public ScheduledSendgridEventFetcher(SendgridEventService sendgridEventService) {
    this.sendgridEventService = sendgridEventService;
  }

  @Scheduled(timeUnit = TimeUnit.MINUTES, fixedDelay = 30, initialDelay = 1)
  @SchedulerLock(
      name = "SendgridEventService.saveSendgridActivity",
      lockAtLeastFor = "1m",
      lockAtMostFor = "15m")
  public void saveSendgridActivity() {
    sendgridEventService.saveSendgridActivity();
  }
}
