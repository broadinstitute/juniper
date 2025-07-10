package bio.terra.pearl.api.participant.controller;

import bio.terra.pearl.api.participant.api.ExternalEventApi;
import bio.terra.pearl.api.participant.model.ExternalEventFailedLoginBody;
import bio.terra.pearl.api.participant.service.ExternalEventExtService;
import bio.terra.pearl.core.model.EnvironmentName;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
public class ExternalEventController implements ExternalEventApi {

  private final ExternalEventExtService externalEventExtService;

  public ExternalEventController(ExternalEventExtService externalEventExtService) {
    this.externalEventExtService = externalEventExtService;
  }

  @Override
  public ResponseEntity<Void> trackFailedLogin(
      String portalShortcode, String envName, ExternalEventFailedLoginBody body) {
    try {
      // track asynchronously; caller doesn't care if it succeeds or fails
      externalEventExtService.trackFailedLoginEventAsync(
          portalShortcode, EnvironmentName.valueOfCaseInsensitive(envName), body.getUsername());
    } catch (Exception e) {
      // Log the exception if tracking fails
      // Note: Logging is not shown here, but should be implemented in a real application
      log.error("Failed to call failed login async method: {}", e.getMessage());
    }

    return ResponseEntity.noContent().build();
  }
}
