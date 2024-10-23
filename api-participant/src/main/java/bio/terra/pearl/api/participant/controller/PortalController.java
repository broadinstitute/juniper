package bio.terra.pearl.api.participant.controller;

import bio.terra.pearl.api.participant.api.PortalApi;
import bio.terra.pearl.api.participant.models.dto.PortalBrandingDto;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.site.LocalizedSiteContent;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.portal.PortalDashboardConfigService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import bio.terra.pearl.core.service.portal.PortalService;
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
      origins = {
        "https://juniperdemodev.b2clogin.com", // Heart Demo (demo)
        "https://juniperdemoprod.b2clogin.com", // Demo Portal (prod)
        "https://junipercmidemo.b2clogin.com", // CMI (demo only)
        "https://juniperrgpdemo.b2clogin.com", // RGP (demo only)
        "https://ourhealthdev.b2clogin.com", // OurHealth (demo)
        "https://ourhealthstudy.b2clogin.com", // OurHealth (prod)
        "https://hearthivedev.b2clogin.com", // HeartHive (demo)
        "https://hearthive.b2clogin.com", // HeartHive (prod)
        "https://gvascdev.b2clogin.com", // gVASC (demo)
        "https://gvascprod.b2clogin.com", // gVASC (prod)
        "https://juniperatcpdev.b2clogin.com", // ATCP (demo)
      },
      maxAge = 3600,
      methods = {RequestMethod.GET, RequestMethod.OPTIONS})
  /*
   * This method is used to get the branding information for a portal environment.
   * Since this is only returning publicly available assets (logos, css attributes, etc),
   * this is allowed to be accessed from other domains. Additionally, the domains are
   * limited to b2c origins that we control.
   */
  public ResponseEntity<Object> getBranding(
      String portalShortcode, String envName, String language) {
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);

    PortalEnvironment portalEnv =
        portalEnvironmentService
            .loadWithParticipantSiteContent(portalShortcode, environmentName, language)
            .orElseThrow(() -> new NotFoundException("Portal environment not found"));

    LocalizedSiteContent siteContent =
        portalEnv.getSiteContent().getLocalizedSiteContents().stream()
            .findFirst()
            .orElseThrow(() -> new NotFoundException("Site content not found"));

    PortalBrandingDto branding =
        PortalBrandingDto.builder()
            .dashboardBackgroundColor(siteContent.getDashboardBackgroundColor())
            .navLogoCleanFileName(siteContent.getNavLogoCleanFileName())
            .navLogoVersion(siteContent.getNavLogoVersion())
            .primaryBrandColor(siteContent.getPrimaryBrandColor())
            .build();

    return ResponseEntity.ok(branding);
  }
}
