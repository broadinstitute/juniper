package bio.terra.pearl.api.admin.service.logging;

import bio.terra.pearl.core.model.log.LogEvent;
import bio.terra.pearl.core.model.log.LogEventType;
import bio.terra.pearl.core.service.LoggingService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class LoggingExtService {
  private final LoggingService loggingService;

  public LoggingExtService(LoggingService loggingService) {
    this.loggingService = loggingService;
  }

  public List<LogEvent> listLogEvents(String days, List<LogEventType> eventTypes) {
    return loggingService.listLogEvents(days, eventTypes);
  }
}
