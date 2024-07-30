package bio.terra.pearl.api.participant.controller;

import bio.terra.pearl.api.participant.api.PublicApi;
import bio.terra.pearl.api.participant.config.B2CConfigurationService;
import bio.terra.pearl.api.participant.config.VersionConfiguration;
import bio.terra.pearl.api.participant.model.SystemStatus;
import bio.terra.pearl.api.participant.model.VersionProperties;
import bio.terra.pearl.api.participant.service.StatusService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.portal.PortalEnvironmentDescriptor;
import bio.terra.pearl.core.model.site.SiteMedia;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.core.service.site.SiteMediaService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class PublicApiController implements PublicApi {
  private final B2CConfigurationService b2CConfigurationService;
  private final SiteMediaService siteMediaService;
  private final PortalService portalService;
  private final StatusService statusService;
  private final VersionConfiguration versionConfiguration;
  private final Environment env;

  @Autowired
  public PublicApiController(
      B2CConfigurationService b2CConfigurationService,
      SiteMediaService siteMediaService,
      PortalService portalService,
      StatusService statusService,
      VersionConfiguration versionConfiguration,
      Environment env) {
    this.b2CConfigurationService = b2CConfigurationService;
    this.siteMediaService = siteMediaService;
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

  @GetMapping(value = "/config")
  public ResponseEntity<Object> getConfig(HttpServletRequest request) {
    Optional<PortalEnvironmentDescriptor> portal = getPortalDescriptorForRequest(request);
    if (portal.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    String portalShortcode = portal.get().shortcode();

    Map<String, String> portalConfig = b2CConfigurationService.getB2CForPortal(portalShortcode);
    if (portalConfig == null || portalConfig.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(portalConfig);
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

  @CrossOrigin(
      origins = {
        "https://juniperdemodev.b2clogin.com", // Heart Demo (demo only)
        "https://junipercmidemo.b2clogin.com", // CMI (demo only)
        "https://juniperrgpdemo.b2clogin.com", // RGP (demo only)
        "https://ourhealthdev.b2clogin.com", // OurHealth (demo)
        "https://ourhealthstudy.b2clogin.com", // OurHealth (prod)
        "https://hearthivedev.b2clogin.com", // HeartHive (demo)
        "https://hearthive.b2clogin.com", // HeartHive (prod)
        "https://gvascdev.b2clogin.com", // gVASC (demo)
        "https://gvascprod.b2clogin.com" // gVASC (prod)
      },
      maxAge = 3600,
      methods = {RequestMethod.GET, RequestMethod.OPTIONS})
  /*
   * This method is used to get the branding information for a portal environment.
   * Since this is only returning publicly available assets (logos, css attributes, etc),
   * this is allowed to be accessed from other domains. Additionally, the domains are
   * limited to b2c origins that we control.
   */
  @GetMapping(value = "/favicon.ico")
  public ResponseEntity<Resource> favicon(HttpServletRequest request) {
    Optional<PortalEnvironmentDescriptor> portal = getPortalDescriptorForRequest(request);
    if (portal.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    Optional<SiteMedia> imageOpt =
        siteMediaService.findOneLatestVersion(portal.get().shortcode(), "favicon.ico");
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
   * <p>Spring now extremely disfavors suffix-matching, and disallows pattern-matching after **. So
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

  /**
   * DANGER: The three methods below essentially tell the server to ignore the fingerprint of the
   * asset requested, and always just return the asset it has. This enables us to do rolling
   * deployments and not have the case where a request for an asset from an old pod gets served by a
   * new pod which has a different asset fingerprint, and thus returns 404. This obviously opens the
   * door for obscure bugs relating to a user having different versions of different frontend assets
   * on the same page. This is reasonably safe for us now, though, since main.js is typically the
   * only asset that changes between versions -- the majority of our css is inlined, and our JS
   * chunks are for things like the privacy policy that are rarely used/updated. And the
   * fingerprints are still included in index.html, so the fingerprints still do their job of
   * preventing unwanted browser caching.
   *
   * <p>We're willing to temporarily accept the risk of possibly odd behavior in exchange for the
   * site not appearing as down during deploys. Eventually, we should upgrade our deployment/hosting
   * infrastructure to solve this problem in a more robust way
   */
  @GetMapping(value = "/assets/index-{hash:[a-zA-Z0-9-_]{8}}.css")
  public String getFingerprintedCss() {
    return "forward:/assets/index.css";
  }

  @GetMapping(value = "/assets/{fileId}-{hash:[a-zA-Z0-9-_]{8}}.js")
  public String getFingerprintedJs(@PathVariable("fileId") String fileId) {
    return "forward:/assets/%s.js".formatted(fileId);
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
