package bio.terra.pearl.api.admin.controller;

import bio.terra.pearl.api.admin.api.SiteImageApi;
import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.api.admin.service.siteContent.SiteImageExtService;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.site.SiteImage;

import java.io.IOException;
import java.net.URLConnection;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class SiteImageController implements SiteImageApi {
  private AuthUtilService authUtilService;
  private HttpServletRequest request;
  private SiteImageExtService siteImageExtService;

  public SiteImageController(
      AuthUtilService authUtilService,
      HttpServletRequest request,
      SiteImageExtService siteImageExtService) {
    this.authUtilService = authUtilService;
    this.request = request;
    this.siteImageExtService = siteImageExtService;
  }

  /**
   * NOTE -- this endpoint is PUBLIC since we need to load the images in preview mode in the admin
   * tool, and the preview mode loads the images as regular html images, and so does not include an
   * auth header.
   */
  @Override
  public ResponseEntity<Resource> get(
      String portalShortcode, String envName, String cleanFileName, Integer version) {
    Optional<SiteImage> siteImageOpt =
        siteImageExtService.findOne(portalShortcode, cleanFileName, version);
    return convertToResourceResponse(siteImageOpt);
  }

  @Override
  public ResponseEntity<Object> list(String portalShortcode) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    return ResponseEntity.ok(siteImageExtService.list(portalShortcode, operator));
  }

  @Override
  public ResponseEntity<Object> upload(
      String portalShortcode, String uploadFileName, Integer version, MultipartFile image) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    try {
      byte[] imageData = image.getBytes();
      return ResponseEntity.ok(
          siteImageExtService.upload(portalShortcode, uploadFileName, imageData, operator));
    } catch (IOException e) {
      throw new IllegalArgumentException("could not read image data");
    }
  }

  private ResponseEntity<Resource> convertToResourceResponse(Optional<SiteImage> imageOpt) {
    if (imageOpt.isPresent()) {
      MediaType contentType;
      SiteImage image = imageOpt.get();
      if (image.getCleanFileName().endsWith(".ico")) {
        contentType = MediaType.valueOf("image/x-icon");
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
