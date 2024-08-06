package bio.terra.pearl.api.admin.controller.logging;

import bio.terra.pearl.api.admin.api.LoggingApi;
import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.auth.context.OperatorAuthContext;
import bio.terra.pearl.api.admin.service.logging.LoggingExtService;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.log.LogEventType;
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
  public ResponseEntity<Object> get(String days, String eventTypes, Integer limit) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    OperatorAuthContext authContext = OperatorAuthContext.of(operator);

    List<LogEventType> logEventTypes =
        Arrays.stream(eventTypes.split(",")).map(LogEventType::valueOf).toList();
    return ResponseEntity.ok(
        loggingExtService.listLogEvents(authContext, days, logEventTypes, limit));
  }
}
