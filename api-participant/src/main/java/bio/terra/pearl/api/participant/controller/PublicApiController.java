package bio.terra.pearl.api.participant.controller;

import bio.terra.pearl.api.participant.api.PublicApi;
import bio.terra.pearl.api.participant.config.B2CConfiguration;
import bio.terra.pearl.api.participant.config.VersionConfiguration;
import bio.terra.pearl.api.participant.model.SystemStatus;
import bio.terra.pearl.api.participant.model.VersionProperties;
import bio.terra.pearl.api.participant.service.StatusService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.portal.PortalEnvironmentId;
import bio.terra.pearl.core.model.site.SiteImage;
import bio.terra.pearl.core.service.site.SiteImageService;
import java.util.Map;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PublicApiController implements PublicApi {
  private final B2CConfiguration b2CConfiguration;
  private final SiteImageService siteImageService;
  private final StatusService statusService;
  private final VersionConfiguration versionConfiguration;
  private final Environment env;

  @Autowired
  public PublicApiController(
      B2CConfiguration b2CConfiguration,
      SiteImageService siteImageService,
      StatusService statusService,
      VersionConfiguration versionConfiguration,
      Environment env) {
    this.b2CConfiguration = b2CConfiguration;
    this.siteImageService = siteImageService;
    this.statusService = statusService;
    this.versionConfiguration = versionConfiguration;
    this.env = env;
  }

  @Override
  public ResponseEntity<SystemStatus> getStatus() {
    SystemStatus systemStatus = statusService.getCurrentStatus();
    HttpStatus httpStatus = systemStatus.isOk() ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
    return new ResponseEntity<>(systemStatus, httpStatus);
  }

  @Override
  public ResponseEntity<VersionProperties> getVersion() {
    VersionProperties currentVersion =
        new VersionProperties()
            .gitTag(versionConfiguration.gitTag())
            .gitHash(versionConfiguration.gitHash())
            .github(versionConfiguration.github())
            .build(versionConfiguration.build());
    return ResponseEntity.ok(currentVersion);
  }

  @Override
  public ResponseEntity<Object> getConfig() {
    var config = buildConfigMap();
    return ResponseEntity.ok(config);
  }

  /**
   * Note that, unlike the admin-api app, we do not map the swagger-ui.html page in production as we
   * don't want participants (or anyone else) attempting to use the participant api directly in
   * production. We still keep this project swagger-ized to allow developers to use the swagger-ui
   * in local development
   */
  @GetMapping(value = "/swagger-ui.html")
  public String getSwagger() {
    if (env.getProperty("swagger.enabled", Boolean.class, false)) {
      return "swagger-ui.html";
    }
    // if not enabled, treat swagger-ui.html just like any other route and let the SPA handle it
    return "forward:/";
  }

  @GetMapping(value = "/favicon.ico")
  public ResponseEntity<Resource> favicon(HttpServletRequest request) {
    PortalEnvironmentId portalEnvId = getPortalForRequest(request);
    Optional<SiteImage> imageOpt =
        siteImageService.findOneLatestVersion(portalEnvId.getShortcode(), "favicon.ico");
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType("image/x-icon"))
        .body(
            imageOpt.isPresent()
                ? new ByteArrayResource(imageOpt.get().getData())
                : new ClassPathResource("images/favicon.ico"));
  }

  /**
   * enable react router to handle all non-api, non-resource paths by routing everything else to the
   * index path. Adapted from
   * https://stackoverflow.com/questions/47689971/how-to-work-with-react-routers-and-spring-boot-controller
   */
  @GetMapping(value = {"/{x:[\\w\\-]+}", "/{x:^(?!api$).*$}/*/{y:[\\w\\-]+}"})
  public String getIndex(HttpServletRequest request) {
    return "forward:/";
  }

  private Map<String, String> buildConfigMap() {
    return Map.of(
        "b2cTenantName", b2CConfiguration.tenantName(),
        "b2cClientId", b2CConfiguration.clientId(),
        "b2cPolicyName", b2CConfiguration.policyName());
  }

  private PortalEnvironmentId getPortalForRequest(HttpServletRequest request) {
    String hostname = request.getServerName();
    String[] parts = hostname.split("\\.");
    try {
      EnvironmentName envName = EnvironmentName.valueOf(parts[0]);
      return PortalEnvironmentId.builder().environmentName(envName).shortcode(parts[1]).build();
    } catch (IllegalArgumentException ex) {
      return PortalEnvironmentId.builder()
          .environmentName(EnvironmentName.live)
          .shortcode(parts[0])
          .build();
    }
  }
}
