package bio.terra.pearl.api.admin.service.notifications;

import bio.terra.pearl.core.dao.notification.SendgridEvent;
import bio.terra.pearl.core.dao.notification.SendgridEventDao;
import bio.terra.pearl.core.service.notification.email.SendgridClient;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SendgridActivityService {
  private static final Logger logger = LoggerFactory.getLogger(SendgridActivityService.class);
  private SendgridClient sendgridClient;
  private SendgridEventDao sendgridEventDao;

  public SendgridActivityService(SendgridClient sendgridClient, SendgridEventDao sendgridEventDao) {
    this.sendgridEventDao = sendgridEventDao;
    this.sendgridClient = sendgridClient;
  }

  public void saveSendgridActivity() throws Exception {
    // Find the most recently recorded event, and get all events since then
    Optional<SendgridEvent> mostRecentEvent = sendgridEventDao.findMostRecentEvent();

    Instant mostRecentEventDate =
        mostRecentEvent
            .map(SendgridEvent::getLastEventTime)
            .orElse(Instant.now().minus(30, ChronoUnit.DAYS));

    List<SendgridEvent> events = getAllRecentSendgridEvents(mostRecentEventDate, Instant.now());
    // Over the course of gathering the results from Sendgrid's paginated API, it's possible that
    // we've accumulated duplicate events at the fringes of the pages. The database will handle
    // this fine, but this could result in the following log message indicating that we've inserted
    // more events than we actually have.
    logger.info("Sendgrid activity query returned {} events", events.size());

    sendgridEventDao.bulkUpsert(events);
  }

  public List<SendgridEvent> getAllRecentSendgridEvents(Instant startDate, Instant endDate)
      throws Exception {
    final int PAGE_SIZE = 1000;
    String queryStartDate = instantToPreferredSendgridDateFormat(startDate);
    String queryEndDate = instantToPreferredSendgridDateFormat(endDate);

    logger.info("Querying for SendGrid activity between {} and {}", queryStartDate, queryEndDate);

    List<SendgridEvent> events = sendgridClient.getEvents(queryStartDate, queryEndDate, PAGE_SIZE);

    if (events.size() == PAGE_SIZE) {
      endDate = events.get(events.size() - 1).getLastEventTime();
      logger.info(
          "Sendgrid activity query returned the limit of {} events. Querying for additional events between {} and {}",
          PAGE_SIZE,
          queryStartDate,
          queryEndDate);
      events.addAll(getAllRecentSendgridEvents(startDate, endDate));
    }

    return events;
  }

  public String instantToPreferredSendgridDateFormat(Instant instant) {
    return instant
        .atOffset(ZoneOffset.UTC)
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
  }
}
