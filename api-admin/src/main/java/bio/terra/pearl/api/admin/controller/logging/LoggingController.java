package bio.terra.pearl.api.admin.controller.logging;

import bio.terra.pearl.api.admin.api.LoggingApi;
import bio.terra.pearl.api.admin.service.logging.LoggingExtService;
import bio.terra.pearl.core.model.log.LogEventType;
import io.micrometer.common.util.StringUtils;
import java.util.Arrays;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class LoggingController implements LoggingApi {

  private final LoggingExtService loggingExtService;

  public LoggingController(LoggingExtService loggingExtService) {
    this.loggingExtService = loggingExtService;
  }

  @Override
  public ResponseEntity<Object> get(String days, String eventTypes) {
    if (StringUtils.isBlank(eventTypes)) {
      return ResponseEntity.ok(List.of());
    }

    List<LogEventType> logEventTypes =
        Arrays.stream(eventTypes.split(",")).map(LogEventType::valueOf).toList();
    System.out.println(logEventTypes);
    return ResponseEntity.ok(loggingExtService.listLogEvents(days, logEventTypes));
  }
}
