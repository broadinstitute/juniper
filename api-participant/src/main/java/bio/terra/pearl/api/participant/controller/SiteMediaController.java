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

@Controller
public class SiteMediaController implements SiteMediaApi {
  private SiteMediaService siteMediaService;

  public SiteMediaController(SiteMediaService siteMediaService) {
    this.siteMediaService = siteMediaService;
  }

  @Override
  public ResponseEntity<Resource> get(
      String portalShortcode, String envName, String cleanFileName, Integer version) {
    Optional<SiteMedia> siteMediaOpt;

    siteMediaOpt = siteMediaService.findOne(portalShortcode, cleanFileName, version);
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
