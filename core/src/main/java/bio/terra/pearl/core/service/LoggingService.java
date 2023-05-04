package bio.terra.pearl.core.service;

import bio.terra.pearl.core.dao.log.LogEventDao;
import bio.terra.pearl.core.model.log.LogEvent;
import bio.terra.pearl.core.model.log.LogEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** logging service for logging error information that might have PII/PHI associated with it. */
@Service
public class LoggingService {
  private LogEventDao logEventDao;

  private static final Logger loggingServiceLogger = LoggerFactory.getLogger(LoggingService.class);

  public LoggingService(LogEventDao logEventDao) {
    this.logEventDao = logEventDao;
  }

  /** creates a log event and sends the non-sensitive fields to container logs */
  public LogEvent createAndSend(LogEvent event) {
    return createAndSend(event, loggingServiceLogger);
  }

  /** creates a log event and sends the non-sensitive fields to container logs, using the passed-in logger. */
  public LogEvent createAndSend(LogEvent event, Logger logger) {
    LogEvent createdEvent = logEventDao.create(event);
    String eventString = formatEventString(createdEvent);
    if (LogEventType.ERROR.equals(event.getEventType())) {
      logger.error(eventString);
    } else {
      logger.info(eventString);
    }
    return createdEvent;
  }

  // return as a string, with detail and trace fields omitted
  public String formatEventString(LogEvent event) {
    return """
      id: {}, createdAt: {}, eventType: {}, eventName {}, eventSource: {}, portalShortcode: {}, 
      studyShortcode: {}, environmentName: {}, enrolleeShortcode: {}, operatorId: {}
      """.formatted(event.getId(), event.getCreatedAt(), event.getEventType(), event.getEventSource(), event.getPortalShortcode(),
        event.getStudyShortcode(), event.getEnvironmentName(), event.getEnrolleeShortcode(), event.getOperatorId());
  }
}
