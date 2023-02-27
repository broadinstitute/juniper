package bio.terra.pearl.api.participant.controller;

import bio.terra.pearl.api.participant.api.PublicApi;
import bio.terra.pearl.api.participant.config.B2CConfiguration;
import bio.terra.pearl.api.participant.config.VersionConfiguration;
import bio.terra.pearl.api.participant.model.SystemStatus;
import bio.terra.pearl.api.participant.model.VersionProperties;
import bio.terra.pearl.api.participant.service.StatusService;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PublicApiController implements PublicApi {
  private final B2CConfiguration b2CConfiguration;
  private final StatusService statusService;
  private final VersionConfiguration versionConfiguration;
  private final Environment env;

  @Autowired
  public PublicApiController(
      B2CConfiguration b2CConfiguration,
      StatusService statusService,
      VersionConfiguration versionConfiguration,
      Environment env) {
    this.b2CConfiguration = b2CConfiguration;
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
        "b2cClientId", b2CConfiguration.clientId());
  }
}
