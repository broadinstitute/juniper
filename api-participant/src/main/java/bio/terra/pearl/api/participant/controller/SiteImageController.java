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
      String portalShortcode, String envName, String imageShortcode) {
    // we ignore the portalShortcode and envName -- those are just in the URL for traceability
    Optional<SiteImage> siteImageOpt = siteImageService.findOne(imageShortcode);
    Optional<Resource> imageResourceOpt = Optional.empty();
    if (siteImageOpt.isPresent()) {
      imageResourceOpt = Optional.of(new ByteArrayResource(siteImageOpt.get().getData()));
    }
    return ResponseEntity.of(imageResourceOpt);
  }
}
