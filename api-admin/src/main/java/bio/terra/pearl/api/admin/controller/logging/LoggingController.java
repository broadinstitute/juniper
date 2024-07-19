package bio.terra.pearl.api.admin.controller.logging;

import bio.terra.pearl.api.admin.api.LoggingApi;
import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.logging.LoggingExtService;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.log.LogEventType;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class LoggingController implements LoggingApi {

  private final LoggingExtService loggingExtService;
  private final AuthUtilService authUtilService;
  private final HttpServletRequest request;

  public LoggingController(
      LoggingExtService loggingExtService,
      AuthUtilService authUtilService,
      HttpServletRequest request) {
    this.loggingExtService = loggingExtService;
    this.authUtilService = authUtilService;
    this.request = request;
  }

  @Override
  public ResponseEntity<Object> get(String days, String eventTypes) {
    AdminUser operator = authUtilService.requireAdminUser(request);

    if (!operator.isSuperuser()) {
      return ResponseEntity.status(404)
          .body("You do not have access to this resource or it does not exist.");
    }

    if (StringUtils.isBlank(eventTypes)) {
      return ResponseEntity.ok(List.of());
    }

    List<LogEventType> logEventTypes =
        Arrays.stream(eventTypes.split(",")).map(LogEventType::valueOf).toList();
    System.out.println(logEventTypes);
    return ResponseEntity.ok(loggingExtService.listLogEvents(days, logEventTypes));
  }
}
