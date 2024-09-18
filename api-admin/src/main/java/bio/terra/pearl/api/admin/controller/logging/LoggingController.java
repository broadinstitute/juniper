package bio.terra.pearl.api.admin.controller.logging;

import bio.terra.pearl.api.admin.api.LoggingApi;
import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.auth.context.OperatorAuthContext;
import bio.terra.pearl.api.admin.service.logging.LoggingExtService;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.log.LogEventType;
import bio.terra.pearl.core.service.logging.MixpanelService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class LoggingController implements LoggingApi {

  private final LoggingExtService loggingExtService;
  private final AuthUtilService authUtilService;
  private final HttpServletRequest request;
  private final MixpanelService mixpanelService;

  public LoggingController(
      LoggingExtService loggingExtService,
      AuthUtilService authUtilService,
      HttpServletRequest request,
      MixpanelService mixpanelService) {
    this.loggingExtService = loggingExtService;
    this.authUtilService = authUtilService;
    this.request = request;
    this.mixpanelService = mixpanelService;
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

  // This method is called by the Mixpanel frontend library to track events.
  @Override
  public ResponseEntity<String> trackEvent(String data) {
    if (StringUtils.isEmpty(data)) {
      // If there is no urlencoded form data, return no content
      return ResponseEntity.noContent().build();
    }
    mixpanelService.logEvent(data);
    return ResponseEntity.accepted().build();
  }

  @Override
  // This stub method is implemented so Mixpanel calls from the frontend do not error
  public ResponseEntity<String> trackEngage(String data) {
    return ResponseEntity.accepted().build();
  }

  @Override
  // This stub method is implemented so Mixpanel calls from the frontend do not error
  public ResponseEntity<String> trackGroups(String data) {
    return ResponseEntity.accepted().build();
  }
}
