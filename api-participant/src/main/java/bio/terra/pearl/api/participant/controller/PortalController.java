package bio.terra.pearl.api.participant.controller;

import bio.terra.pearl.api.participant.api.PortalApi;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.service.portal.PortalService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class PortalController implements PortalApi {
  private PortalService portalService;
  private ObjectMapper objectMapper;

  public PortalController(PortalService portalService, ObjectMapper objectMapper) {
    this.portalService = portalService;
    this.objectMapper = objectMapper;
  }

  @Override
  public ResponseEntity<Object> get(String portalShortcode, String envName) {
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    Optional<Portal> portalOpt =
        portalService.loadWithParticipantSiteContent(portalShortcode, environmentName, "en");
    return ResponseEntity.of(portalOpt.map(portal -> portal));
  }
}
