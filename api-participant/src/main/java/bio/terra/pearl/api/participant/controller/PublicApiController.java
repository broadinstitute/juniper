package bio.terra.pearl.api.participant.controller;

import bio.terra.pearl.api.participant.api.PublicApi;
import bio.terra.pearl.api.participant.config.B2CConfiguration;
import bio.terra.pearl.api.participant.config.VersionConfiguration;
import bio.terra.pearl.api.participant.model.SystemStatus;
import bio.terra.pearl.api.participant.model.VersionProperties;
import bio.terra.pearl.api.participant.service.StatusService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.portal.PortalEnvironmentDescriptor;
import bio.terra.pearl.core.model.site.SiteImage;
import bio.terra.pearl.core.service.portal.PortalService;
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
  private final PortalService portalService;
  private final StatusService statusService;
  private final VersionConfiguration versionConfiguration;
  private final Environment env;

  @Autowired
  public PublicApiController(
      B2CConfiguration b2CConfiguration,
      SiteImageService siteImageService,
      PortalService portalService,
      StatusService statusService,
      VersionConfiguration versionConfiguration,
      Environment env) {
    this.b2CConfiguration = b2CConfiguration;
    this.siteImageService = siteImageService;
    this.portalService = portalService;
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
    Optional<PortalEnvironmentDescriptor> portal = getPortalDescriptorForRequest(request);
    if (portal.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    Optional<SiteImage> imageOpt =
        siteImageService.findOneLatestVersion(portal.get().shortcode(), "favicon.ico");
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
   *
   * Spring now extremely disfavors suffix-matching, and disallows pattern-matching after **. So
   * we've enabled up to 12 layers of route-nesting -- hopefully we don't need more. One option
   * would be to re-enable legacy ant pattern matching, but the Spring docs suggest that, as ugly as
   * the patterns below are, they are more secure and performant than a matcher like "** /foo" since
   * the matcher can immediately make decisions based on number of segments. See
   * https://github.com/spring-projects/spring-framework/issues/19112 and general discussion of
   * PathPatternParser
   */
  @GetMapping(
      value = {
        "/{x:[\\w\\-]+}",
        "/{x:^(?!api$).*$}/{y:[\\w\\-]+}",
        "/{x:^(?!api$).*$}/*/{y:[\\w\\-]+}",
        "/{x:^(?!api$).*$}/*/*/{y:[\\w\\-]+}",
        "/{x:^(?!api$).*$}/*/*/*/{y:[\\w\\-]+}",
        "/{x:^(?!api$).*$}/*/*/*/*/{y:[\\w\\-]+}",
        "/{x:^(?!api$).*$}/*/*/*/*/*/{y:[\\w\\-]+}",
        "/{x:^(?!api$).*$}/*/*/*/*/*/*/{y:[\\w\\-]+}",
        "/{x:^(?!api$).*$}/*/*/*/*/*/*/*/{y:[\\w\\-]+}",
        "/{x:^(?!api$).*$}/*/*/*/*/*/*/*/*/{y:[\\w\\-]+}",
        "/{x:^(?!api$).*$}/*/*/*/*/*/*/*/*/*/{y:[\\w\\-]+}",
        "/{x:^(?!api$).*$}/*/*/*/*/*/*/*/*/*/*/{y:[\\w\\-]+}",
        "/{x:^(?!api$).*$}/*/*/*/*/*/*/*/*/*/*/*/{y:[\\w\\-]+}",
        "/{x:^(?!api$).*$}/*/*/*/*/*/*/*/*/*/*/*/*/{y:[\\w\\-]+}"
      })
  public String getIndex(HttpServletRequest request) {
    return "forward:/";
  }

  private Map<String, String> buildConfigMap() {
    return Map.of(
        "b2cTenantName", b2CConfiguration.tenantName(),
        "b2cClientId", b2CConfiguration.clientId(),
        "b2cPolicyName", b2CConfiguration.policyName());
  }

  private Optional<PortalEnvironmentDescriptor> getPortalDescriptorForRequest(
      HttpServletRequest request) {
    String hostname = request.getServerName();
    String[] parts = hostname.split("\\.");

    Optional<EnvironmentName> envNameOpt = EnvironmentName.optionalValueOfCaseInsensitive(parts[0]);
    EnvironmentName envName = EnvironmentName.live;
    String shortcodeOrHostname = parts[0];
    if (envNameOpt.isPresent()) {
      envName = envNameOpt.get();
      shortcodeOrHostname = parts[1];
    }
    Optional<Portal> portal = portalService.findOneByShortcodeOrHostname(shortcodeOrHostname);
    if (portal.isPresent()) {
      return Optional.of(new PortalEnvironmentDescriptor(portal.get().getShortcode(), envName));
    }
    return Optional.empty();
  }
}
