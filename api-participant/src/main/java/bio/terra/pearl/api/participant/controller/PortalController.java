package bio.terra.pearl.api.participant.controller;

import bio.terra.pearl.api.participant.api.PortalApi;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.portal.PortalDashboardConfigService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import bio.terra.pearl.core.service.portal.PortalService;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class PortalController implements PortalApi {
  private PortalService portalService;
  private PortalEnvironmentService portalEnvironmentService;
  private PortalDashboardConfigService portalDashboardConfigService;

  public PortalController(
      PortalService portalService,
      PortalEnvironmentService portalEnvironmentService,
      PortalDashboardConfigService portalDashboardConfigService) {
    this.portalService = portalService;
    this.portalEnvironmentService = portalEnvironmentService;
    this.portalDashboardConfigService = portalDashboardConfigService;
  }

  @Override
  public ResponseEntity<Object> get(String portalShortcode, String envName, String language) {
    if (StringUtils.isBlank(language)) {
      // TODO (JN-863): Use the default language
      language = "en";
    }
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    Optional<Portal> portalOpt =
        portalService.loadWithParticipantSiteContent(portalShortcode, environmentName, language);
    return ResponseEntity.of(portalOpt.map(portal -> portal));
  }

  @Override
  public ResponseEntity<Object> listPortalEnvAlerts(String portalShortcode, String envName) {
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    PortalEnvironment portalEnvironment =
        portalEnvironmentService
            .findOne(portalShortcode, environmentName)
            .orElseThrow(() -> new NotFoundException("Portal environment not found"));

    return ResponseEntity.ok(
        portalDashboardConfigService.findByPortalEnvId(portalEnvironment.getId()));
  }
}
