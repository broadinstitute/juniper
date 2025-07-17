package bio.terra.pearl.api.participant.controller;

import bio.terra.pearl.api.participant.api.ExternalEventApi;
import bio.terra.pearl.api.participant.model.ExternalEventFailedLoginBody;
import bio.terra.pearl.api.participant.service.B2cEventExtService;
import bio.terra.pearl.core.model.EnvironmentName;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@Slf4j
public class B2cEventController implements ExternalEventApi {

  private final B2cEventExtService b2cEventExtService;

  public B2cEventController(B2cEventExtService b2cEventExtService) {
    this.b2cEventExtService = b2cEventExtService;
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
        "https://juniperatcp.b2clogin.com", // ATCP (prod)
        "https://trccproject.b2clogin.com" // tRCC (prod)
      },
      maxAge = 3600,
      methods = {RequestMethod.GET, RequestMethod.OPTIONS})
  public ResponseEntity<Void> trackFailedLogin(
      String portalShortcode, String envName, ExternalEventFailedLoginBody body) {
    try {
      // track asynchronously; caller doesn't care if it succeeds or fails
      b2cEventExtService.trackFailedLoginEventAsync(
          portalShortcode, EnvironmentName.valueOfCaseInsensitive(envName), body.getUsername());
    } catch (Exception e) {
      log.error("Failed to call failed login async method: {}", e.getMessage());
    }

    return ResponseEntity.noContent().build();
  }
}
