package bio.terra.pearl.api.admin.controller;

import bio.terra.pearl.api.admin.api.PublicApi;
import bio.terra.pearl.api.admin.config.VersionConfiguration;
import bio.terra.pearl.api.admin.model.SystemStatus;
import bio.terra.pearl.api.admin.model.VersionProperties;
import bio.terra.pearl.api.admin.service.StatusService;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PublicApiController implements PublicApi {
  private final StatusService statusService;
  private final VersionConfiguration versionConfiguration;

  @Autowired
  public PublicApiController(
      StatusService statusService, VersionConfiguration versionConfiguration) {
    this.statusService = statusService;
    this.versionConfiguration = versionConfiguration;
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

  /** map the swagger ui page */
  @GetMapping(value = "/swagger-ui.html")
  public String getSwagger() {
    return "swagger-ui.html";
  }

  /**
   * enable react router to handle all non-api, non-resource paths by routing everything else to the
   * index path
   * adapted from https://stackoverflow.com/questions/47689971/how-to-work-with-react-routers-and-spring-boot-controller
   */
  @GetMapping(value = {"/{x:[\\w\\-]+}", "/{x:^(?!api$).*$}/*/{y:[\\w\\-]+}"})
  public String getIndex(HttpServletRequest request) {
    return "forward:/";
  }
}
