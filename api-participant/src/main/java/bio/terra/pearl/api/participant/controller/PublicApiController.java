package bio.terra.pearl.api.participant.controller;

import bio.terra.pearl.api.participant.api.PublicApi;
import bio.terra.pearl.api.participant.config.VersionConfiguration;
import bio.terra.pearl.api.participant.model.SystemStatus;
import bio.terra.pearl.api.participant.model.VersionProperties;
import bio.terra.pearl.api.participant.service.StatusService;
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

  @GetMapping(value = "/")
  public String index() {
    return "redirect:swagger-ui.html";
  }

  @GetMapping(value = "/swagger-ui.html")
  public String getSwagger() {
    return "index";
  }
}
