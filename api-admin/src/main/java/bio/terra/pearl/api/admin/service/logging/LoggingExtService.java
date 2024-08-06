package bio.terra.pearl.api.admin.service.logging;

import bio.terra.pearl.api.admin.service.auth.SuperuserOnly;
import bio.terra.pearl.api.admin.service.auth.context.OperatorAuthContext;
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

  @SuperuserOnly
  public List<LogEvent> listLogEvents(
      OperatorAuthContext authContext, String days, List<LogEventType> eventTypes, Integer limit) {
    if (eventTypes.isEmpty()) {
      return List.of();
    }

    return loggingService.listLogEvents(days, eventTypes, limit);
  }
}
