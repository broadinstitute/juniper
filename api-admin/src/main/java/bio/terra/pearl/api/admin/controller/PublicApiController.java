package bio.terra.pearl.api.admin.controller;

import bio.terra.pearl.api.admin.api.PublicApi;
import bio.terra.pearl.api.admin.config.VersionConfiguration;
import bio.terra.pearl.api.admin.model.SystemStatus;
import bio.terra.pearl.api.admin.model.VersionProperties;
import bio.terra.pearl.api.admin.service.ConfigExtService;
import bio.terra.pearl.api.admin.service.StatusService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class PublicApiController implements PublicApi {
  private final StatusService statusService;
  private final VersionConfiguration versionConfiguration;
  private ConfigExtService configExtService;

  @Autowired
  public PublicApiController(
      StatusService statusService,
      VersionConfiguration versionConfiguration,
      ConfigExtService configExtService) {
    this.statusService = statusService;
    this.versionConfiguration = versionConfiguration;
    this.configExtService = configExtService;
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
    var config = configExtService.getConfigMap();
    return ResponseEntity.ok(config);
  }

  @GetMapping(value = "/config.json")
  public ModelAndView getConfigJson() {
    var config = configExtService.getConfigMap();
    return new ModelAndView("config.json").addAllObjects(config);
  }

  /** map the swagger ui page */
  @GetMapping(value = "/swagger-ui.html")
  public String getSwagger() {
    return "swagger-ui.html";
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
}
