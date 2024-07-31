package bio.terra.pearl.api.participant.controller;

import bio.terra.pearl.api.participant.api.EventApi;
import bio.terra.pearl.core.service.events.MixpanelService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class EventLoggingController implements EventApi {
  private final MixpanelService mixpanelService;

  public EventLoggingController(MixpanelService mixpanelService) {
    this.mixpanelService = mixpanelService;
  }

  @Override
  public ResponseEntity<String> trackEvent(String data) {
    mixpanelService.logEvent(data);
    return ResponseEntity.accepted().build();
  }

  @Override
  public ResponseEntity<String> trackEngage(String data) {
    return ResponseEntity.accepted().build();
  }

  @Override
  public ResponseEntity<String> trackGroups(String data) {
    return ResponseEntity.accepted().build();
  }
}
