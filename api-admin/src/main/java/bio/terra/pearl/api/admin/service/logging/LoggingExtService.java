package bio.terra.pearl.api.admin.service.logging;

import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.log.LogEvent;
import bio.terra.pearl.core.model.log.LogEventType;
import bio.terra.pearl.core.service.LoggingService;
import java.util.List;

import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import org.springframework.stereotype.Service;

@Service
public class LoggingExtService {
  private final LoggingService loggingService;

  public LoggingExtService(LoggingService loggingService) {
    this.loggingService = loggingService;
  }

  public List<LogEvent> listLogEvents(String days, List<LogEventType> eventTypes, AdminUser operator) {
    if (!operator.isSuperuser()) {
      throw new PermissionDeniedException("You do not have permission for this operation");
    }

    if (eventTypes.isEmpty()) {
      return List.of();
    }

    return loggingService.listLogEvents(days, eventTypes);
  }
}
