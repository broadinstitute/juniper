package bio.terra.pearl.core.service;

import bio.terra.pearl.core.dao.log.LogEventDao;
import bio.terra.pearl.core.model.log.LogEvent;
import bio.terra.pearl.core.model.log.LogEventType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

/** logging service for logging error information that might have PII/PHI associated with it. */
@Service
@Slf4j
public class LoggingService {
  private LogEventDao logEventDao;
  private ObjectMapper filteredEventMapper;

  public LoggingService(LogEventDao logEventDao, ObjectMapper objectMapper) {
    this.logEventDao = logEventDao;
    this.filteredEventMapper = new ObjectMapper();
    filteredEventMapper.findAndRegisterModules();
    SimpleBeanPropertyFilter theFilter = SimpleBeanPropertyFilter
        .serializeAllExcept("eventDetail", "stackTrace");
    FilterProvider filters = new SimpleFilterProvider()
        .addFilter("propertyFilter", theFilter);
    objectMapper.setFilterProvider(filters);
  }

  /** creates a log event and sends the non-sensitive fields to container logs */
  public LogEvent createAndSend(LogEvent event) throws JsonProcessingException {
    return createAndSend(event, log);
  }

  /** creates a log event and sends the non-sensitive fields to container logs, using the passed-in logger. */
  public LogEvent createAndSend(LogEvent event, Logger logger) throws JsonProcessingException {
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
  public String formatEventString(LogEvent event) throws JsonProcessingException {
    return filteredEventMapper.writeValueAsString(event);
  }
}
