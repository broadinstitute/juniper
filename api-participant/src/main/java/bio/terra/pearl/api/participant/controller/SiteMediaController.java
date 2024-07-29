package bio.terra.pearl.api.participant.controller;

import bio.terra.pearl.api.participant.api.SiteMediaApi;
import bio.terra.pearl.core.model.site.SiteMedia;
import bio.terra.pearl.core.service.site.SiteMediaService;
import java.net.URLConnection;
import java.util.Optional;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class SiteMediaController implements SiteMediaApi {
  private final SiteMediaService siteMediaService;

  public SiteMediaController(SiteMediaService siteMediaService) {
    this.siteMediaService = siteMediaService;
  }

  @Override
  @CrossOrigin(
      origins = {
        "https://juniperdemodev.b2clogin.com", // Heart Demo (demo only)
        "https://junipercmidemo.b2clogin.com", // CMI (demo only)
        "https://juniperrgpdemo.b2clogin.com", // RGP (demo only)
        "https://ourhealthdev.b2clogin.com", // OurHealth (prod)
        "https://ourhealthstudy.b2clogin.com", // OurHealth (demo)
        "https://hearthivedev.b2clogin.com", // HeartHive (prod)
        "https://hearthive.b2clogin.com", // HeartHive (demo)
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
  public ResponseEntity<Resource> get(
      String portalShortcode, String envName, String cleanFileName, Integer version) {
    Optional<SiteMedia> siteMediaOpt;

    siteMediaOpt = siteMediaService.findOne(portalShortcode, cleanFileName.toLowerCase(), version);
    return convertToResourceResponse(siteMediaOpt);
  }

  @Override
  public ResponseEntity<Resource> getLegacy(
      String portalShortcode, String envName, String cleanFileName, Integer version) {
    return get(portalShortcode, envName, cleanFileName, version);
  }

  private ResponseEntity<Resource> convertToResourceResponse(Optional<SiteMedia> imageOpt) {
    if (imageOpt.isPresent()) {
      MediaType contentType;
      SiteMedia image = imageOpt.get();
      if (image.getCleanFileName().endsWith(".json")) {
        contentType = MediaType.APPLICATION_JSON;
      } else {
        contentType =
            MediaType.parseMediaType(
                URLConnection.guessContentTypeFromName(image.getCleanFileName()));
      }
      return ResponseEntity.ok()
          .contentType(contentType)
          .body(new ByteArrayResource(image.getData()));
    } else {
      return ResponseEntity.notFound().build();
    }
  }
}
