package bio.terra.pearl.api.participant.controller;

import bio.terra.pearl.api.participant.api.PortalApi;
import bio.terra.pearl.api.participant.config.B2CConfigurationService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.site.LocalizedSiteContent;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.portal.PortalDashboardConfigService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import bio.terra.pearl.core.service.portal.PortalService;
import java.util.HashMap;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class PortalController implements PortalApi {
  private final PortalService portalService;
  private final PortalEnvironmentService portalEnvironmentService;
  private final PortalDashboardConfigService portalDashboardConfigService;
  private final B2CConfigurationService b2CConfigurationService;

  public PortalController(
      PortalService portalService,
      PortalEnvironmentService portalEnvironmentService,
      PortalDashboardConfigService portalDashboardConfigService,
      B2CConfigurationService b2CConfigurationService) {
    this.portalService = portalService;
    this.portalEnvironmentService = portalEnvironmentService;
    this.portalDashboardConfigService = portalDashboardConfigService;
    this.b2CConfigurationService = b2CConfigurationService;
  }

  @Override
  public ResponseEntity<Object> get(String portalShortcode, String envName, String language) {
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

  @Override
  @CrossOrigin(
      origins = {"https://juniperdemodev.b2clogin.com", "https://junipercmidemo.b2clogin.com"},
      maxAge = 3600,
      methods = {RequestMethod.GET, RequestMethod.OPTIONS})
  public ResponseEntity<Object> getBranding(String portalShortcode, String envName) {
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);

    Portal portal =
        portalService.loadWithParticipantSiteContent(portalShortcode, environmentName, "en").get();
    PortalEnvironment portalEnv =
        portal.getPortalEnvironments().stream()
            .filter(env -> env.getEnvironmentName().equals(environmentName))
            .findFirst()
            .orElseThrow(() -> new NotFoundException("Portal environment not found"));

    LocalizedSiteContent siteContent =
        portalEnv.getSiteContent().getLocalizedSiteContents().stream()
            .findFirst()
            .orElseThrow(() -> new NotFoundException("Site content not found"));

    HashMap<String, String> branding = new HashMap<>();
    branding.put("dashboardBackgroundColor", siteContent.getDashboardBackgroundColor());
    branding.put("navLogoCleanFileName", siteContent.getNavLogoCleanFileName());
    branding.put("navLogoVersion", String.valueOf(siteContent.getNavLogoVersion()));
    branding.put("primaryBrandColor", siteContent.getPrimaryBrandColor());

    return ResponseEntity.ok(branding);
  }
}
