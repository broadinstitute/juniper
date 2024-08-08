package bio.terra.pearl.api.participant.controller;

import bio.terra.pearl.api.participant.api.LoggingApi;
import bio.terra.pearl.core.model.log.LogEvent;
import bio.terra.pearl.core.model.log.LogEventSource;
import bio.terra.pearl.core.service.LoggingService;
import bio.terra.pearl.core.service.logging.MixpanelService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class LoggingController implements LoggingApi {
  private final LoggingService loggingService;
  private final ObjectMapper objectMapper;
  private final MixpanelService mixpanelService;

  public LoggingController(
      LoggingService loggingService, ObjectMapper objectMapper, MixpanelService mixpanelService) {
    this.loggingService = loggingService;
    this.objectMapper = objectMapper;
    this.mixpanelService = mixpanelService;
  }

  @Override
  public ResponseEntity<String> log(Object body) {
    LogEvent logEvent = objectMapper.convertValue(body, LogEvent.class);
    logEvent.setEventSource(LogEventSource.PARTICIPANT_UI);
    try {
      LogEvent created = loggingService.createAndSend(logEvent);
      // return the id in case the UI wants to include it in troubleshooting guidance
      return ResponseEntity.ok(created.getId().toString());
    } catch (JsonProcessingException e) {
      return ResponseEntity.unprocessableEntity().body(e.getMessage());
    }
  }

  /*
   * This method is called by the Mixpanel frontend library to track events. This differs from the log
   * method above, which saves the log event to the database rather than sending it to Mixpanel.
   */
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
