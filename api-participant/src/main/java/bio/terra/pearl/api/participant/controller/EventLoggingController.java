package bio.terra.pearl.api.participant.controller;

import bio.terra.pearl.api.participant.api.EventLoggingApi;
import bio.terra.pearl.core.service.events.MixpanelService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class EventLoggingController implements EventLoggingApi {
  private final MixpanelService mixpanelService;

  public EventLoggingController(MixpanelService mixpanelService) {
    this.mixpanelService = mixpanelService;
  }

  @Override
  public ResponseEntity<String> logEvent(String data) {
    mixpanelService.logEvent(data);
    return ResponseEntity.accepted().build();
  }
}
