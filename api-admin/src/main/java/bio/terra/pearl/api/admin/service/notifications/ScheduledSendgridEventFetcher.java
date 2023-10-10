package bio.terra.pearl.api.admin.service.notifications;

import java.util.concurrent.TimeUnit;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/*
 * This class is responsible for fetching Sendgrid events from the Sendgrid API and storing them. Sendgrid
 * only stores 30 days of email activity history, and we don't want to lose any of that information. This is
 * important both for auditability and for debugging purposes. This fetcher is scheduled to run every 30 minutes,
 * and it will fetch all events since the last recorded event.
 */

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
