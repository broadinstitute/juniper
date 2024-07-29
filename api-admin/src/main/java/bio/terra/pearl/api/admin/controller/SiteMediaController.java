package bio.terra.pearl.api.admin.controller;

import bio.terra.pearl.api.admin.api.SiteMediaApi;
import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.siteContent.SiteMediaExtService;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.site.SiteMedia;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URLConnection;
import java.util.Optional;
import java.util.UUID;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class SiteMediaController implements SiteMediaApi {
  private AuthUtilService authUtilService;
  private HttpServletRequest request;
  private SiteMediaExtService siteMediaExtService;

  public SiteMediaController(
      AuthUtilService authUtilService,
      HttpServletRequest request,
      SiteMediaExtService siteMediaExtService) {
    this.authUtilService = authUtilService;
    this.request = request;
    this.siteMediaExtService = siteMediaExtService;
  }

  /**
   * NOTE -- this endpoint is PUBLIC since we need to load the images in preview mode in the admin
   * tool, and the preview mode loads the images as regular html images, and so does not include an
   * auth header.
   */
  @Override
  public ResponseEntity<Resource> get(
      String portalShortcode, String envName, String cleanFileName, String version) {
    cleanFileName = cleanFileName.toLowerCase();
    if (version.equalsIgnoreCase("latest")) {
      Optional<SiteMedia> siteMediaOpt =
          siteMediaExtService.findLatest(portalShortcode, cleanFileName);
      return convertToResourceResponse(siteMediaOpt);
    }

    int versionInt;
    try {
      versionInt = Integer.parseInt(version);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("version must be an integer or 'latest'");
    }

    Optional<SiteMedia> siteMediaOpt =
        siteMediaExtService.findOne(portalShortcode, cleanFileName, versionInt);
    return convertToResourceResponse(siteMediaOpt);
  }

  @Override
  public ResponseEntity<Object> list(String portalShortcode) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    return ResponseEntity.ok(siteMediaExtService.list(portalShortcode, operator));
  }

  @Override
  public ResponseEntity<Object> upload(
      String portalShortcode, String uploadFileName, Integer version, MultipartFile image) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    try {
      byte[] imageData = image.getBytes();
      return ResponseEntity.ok(
          siteMediaExtService.upload(portalShortcode, uploadFileName, imageData, operator));
    } catch (IOException e) {
      throw new IllegalArgumentException("could not read image data");
    }
  }

  @Override
  public ResponseEntity<Void> delete(String portalShortcode, UUID id) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    siteMediaExtService.delete(portalShortcode, id, operator);

    return ResponseEntity.ok().build();
  }

  private ResponseEntity<Resource> convertToResourceResponse(Optional<SiteMedia> imageOpt) {
    if (imageOpt.isPresent()) {
      MediaType contentType;
      SiteMedia image = imageOpt.get();
      if (image.getCleanFileName().endsWith(".ico")) {
        contentType = MediaType.valueOf("image/x-icon");
      } else if (image.getCleanFileName().endsWith(".json")) {
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
