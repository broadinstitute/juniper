package bio.terra.pearl.api.participant.controller;

import bio.terra.pearl.api.participant.api.SiteImageApi;
import bio.terra.pearl.core.model.site.SiteImage;
import bio.terra.pearl.core.service.site.SiteImageService;
import java.util.Optional;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class SiteImageController implements SiteImageApi {
  private SiteImageService siteImageService;

  public SiteImageController(SiteImageService siteImageService) {
    this.siteImageService = siteImageService;
  }

  @Override
  public ResponseEntity<Resource> get(
      String portalShortcode, String envName, String cleanFileName, Integer version) {
    Optional<SiteImage> siteImageOpt;

    siteImageOpt = siteImageService.findOne(portalShortcode, cleanFileName, version);
    return convertToResourceResponse(siteImageOpt);
  }

  private ResponseEntity<Resource> convertToResourceResponse(Optional<SiteImage> imageOpt) {
    Optional<Resource> imageResourceOpt = Optional.empty();
    if (imageOpt.isPresent()) {
      SiteImage image = imageOpt.get();
      if (image.getCleanFileName().endsWith(".svg")) {
        imageResourceOpt = Optional.of(new Resource(imageOpt.get().getData()));
      } else {
        imageResourceOpt = Optional.of(new ByteArrayResource(imageOpt.get().getData()));
      }
    }
    return ResponseEntity.of(imageResourceOpt);
  }
}
