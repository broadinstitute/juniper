package bio.terra.pearl.api.participant.controller;

import bio.terra.pearl.api.participant.api.LoggingApi;
import bio.terra.pearl.core.model.log.LogEvent;
import bio.terra.pearl.core.model.log.LogEventSource;
import bio.terra.pearl.core.service.LoggingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class LoggingController implements LoggingApi {
  private LoggingService loggingService;
  private ObjectMapper objectMapper;

  public LoggingController(LoggingService loggingService, ObjectMapper objectMapper) {
    this.loggingService = loggingService;
    this.objectMapper = objectMapper;
  }

  @Override
  public ResponseEntity<Object> log(Object body) {
    LogEvent logEvent = objectMapper.convertValue(body, LogEvent.class);
    logEvent.setEventSource(LogEventSource.PARTICIPANT_UI);
    LogEvent created = loggingService.createAndSend(logEvent);
    // return the id in case the UI wants to include it in troubleshooting guidance
    return ResponseEntity.ok(created.getId());
  }
}
