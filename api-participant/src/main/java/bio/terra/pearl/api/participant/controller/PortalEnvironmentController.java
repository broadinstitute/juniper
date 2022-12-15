package bio.terra.pearl.api.participant.controller;

import bio.terra.pearl.api.participant.api.PortalApi;
import bio.terra.pearl.api.participant.model.PortalEnvDto;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class PortalEnvironmentController implements PortalApi {
  private PortalEnvironmentService portalEnvService;
  private ObjectMapper objectMapper;

  public PortalEnvironmentController(
      PortalEnvironmentService portalEnvService, ObjectMapper objectMapper) {
    this.portalEnvService = portalEnvService;
    this.objectMapper = objectMapper;
  }

  @Override
  public ResponseEntity<PortalEnvDto> get(String portalShortcode, String envName) {
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    Optional<PortalEnvironment> portalEnvOpt =
        portalEnvService.loadOneWithSiteContent(portalShortcode, environmentName, "en");
    PortalEnvDto portalEnvDto =
        objectMapper.convertValue(portalEnvOpt.orElse(null), PortalEnvDto.class);
    return ResponseEntity.of(Optional.of(portalEnvDto));
  }
}
