package bio.terra.pearl.api.admin.service.notifications;

import bio.terra.pearl.core.dao.notification.NotificationDao;
import bio.terra.pearl.core.dao.notification.SendgridEventDao;
import bio.terra.pearl.core.model.notification.Notification;
import bio.terra.pearl.core.model.notification.SendgridEvent;
import bio.terra.pearl.core.service.notification.email.SendgridClient;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class SendgridEventService {
  private final SendgridClient sendgridClient;
  private final SendgridEventDao sendgridEventDao;
  private final NotificationDao notificationDao;

  public SendgridEventService(
      SendgridClient sendgridClient,
      SendgridEventDao sendgridEventDao,
      NotificationDao notificationDao) {
    this.sendgridEventDao = sendgridEventDao;
    this.sendgridClient = sendgridClient;
    this.notificationDao = notificationDao;
  }

  @Transactional
  public void saveSendgridActivity() {
    // Find the most recently recorded event, and get all events since then
    Optional<SendgridEvent> mostRecentEvent = sendgridEventDao.findMostRecentEvent();

    // If we've never recorded an event, default to 30 days ago. Sendgrid only keeps
    // 30 days of activity history, so this will backfill as much history as possible.
    Instant mostRecentEventDate =
        mostRecentEvent
            .map(SendgridEvent::getLastEventTime)
            .orElse(Instant.now().minus(30, ChronoUnit.DAYS));

    List<SendgridEvent> events = getAllRecentSendgridEvents(mostRecentEventDate, Instant.now());
    // Over the course of gathering the results from Sendgrid's paginated API, it's possible that
    // we've accumulated duplicate events at the fringes of the pages. The database will handle
    // this fine, but this could result in the following log message indicating that we've inserted
    // more events than we actually have.
    log.info("Sendgrid activity query returned {} events", events.size());

    List<SendgridEvent> correlatedEvents = correlateNotificationsAndEvents(events);
    bulkUpsert(correlatedEvents);
  }

  /*
   * Correlates Sendgrid events with notifications using the API request ID (X-Message-ID)
   * This currently relies on the fact that we do not batch Sendgrid messages. If we ever start
   * batching messages, we'll need to update our correlation logic to be a bit more sophisticated,
   * because the API request ID will be the same for all messages in a batch. The Sendgrid Event
   * Webhook may be useful in this case. In the meantime, this is fine for MVP.
   */
  public List<SendgridEvent> correlateNotificationsAndEvents(List<SendgridEvent> events) {
    List<String> sendGridApiRequestIds =
        events.stream().map(SendgridEvent::getApiRequestId).toList();
    List<Notification> notifications =
        notificationDao.findAllBySendgridApiRequestId(sendGridApiRequestIds);

    for (SendgridEvent event : events) {
      List<Notification> notificationsForEvent =
          notifications.stream()
              .filter(
                  n ->
                      n.getSendgridApiRequestId() != null
                          && n.getSendgridApiRequestId().equals(event.getApiRequestId()))
              .toList();
      if (notificationsForEvent.size() > 1) {
        // This could happen if we start batching emails without updating the correlation logic.
        // We'll log an error here so we know when this happens, and we won't correlate the event.
        log.error(
            "More than one notification found for Sendgrid api request id: {}",
            event.getApiRequestId());
      } else {
        Optional<Notification> notificationOpt = notificationsForEvent.stream().findFirst();
        notificationOpt.ifPresent(n -> event.setNotificationId(n.getId()));
      }
    }

    return events;
  }

  @Transactional
  public void bulkUpsert(List<SendgridEvent> events) {
    sendgridEventDao.bulkUpsert(events);
  }

  public List<SendgridEvent> getAllRecentSendgridEvents(Instant startDate, Instant endDate) {
    final int PAGE_SIZE = 1000; // This is the maximum page size allowed by SendGrid

    log.info("Querying for SendGrid activity between {} and {}", startDate, endDate);

    List<SendgridEvent> events;
    try {
      events = sendgridClient.getEvents(startDate, endDate, PAGE_SIZE);
    } catch (Exception e) {
      throw new RuntimeException("Unable to query SendGrid for activity: " + e.getMessage());
    }

    if (events.size() == PAGE_SIZE) {
      endDate = events.get(events.size() - 1).getLastEventTime();
      log.info(
          "Sendgrid activity query returned the limit of {} events. Querying for additional events between {} and {}",
          PAGE_SIZE,
          startDate,
          endDate);
      events.addAll(getAllRecentSendgridEvents(startDate, endDate));
    }

    return events;
  }
}
