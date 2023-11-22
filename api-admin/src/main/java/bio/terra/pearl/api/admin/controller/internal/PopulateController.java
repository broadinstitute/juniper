package bio.terra.pearl.api.admin.controller.internal;

import bio.terra.common.exception.InternalServerErrorException;
import bio.terra.pearl.api.admin.api.PopulateApi;
import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.api.admin.service.PopulateExtService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

/**
 * Note this controller does not explicitly validate the safety of the passed-in filenames. Rather,
 * it relies on FilePopulateService.getInputStream to ensure that only files from within the seed
 * directory are allowed to be read.
 */
@Controller
public class PopulateController implements PopulateApi {
  private PopulateExtService populateExtService;
  private HttpServletRequest request;
  private AuthUtilService authUtilService;

  public PopulateController(
      PopulateExtService populateExtService,
      HttpServletRequest request,
      AuthUtilService authUtilService) {
    this.populateExtService = populateExtService;
    this.request = request;
    this.authUtilService = authUtilService;
  }

  @Override
  public ResponseEntity<Object> populateBaseSeed() {
    AdminUser user = authUtilService.requireAdminUser(request);
    var populatedObj = populateExtService.populateBaseSeed(user);
    return ResponseEntity.ok(populatedObj);
  }

  @Override
  public ResponseEntity<Object> populateAdminConfig(Boolean overwrite) {
    AdminUser user = authUtilService.requireAdminUser(request);
    var populatedObj = populateExtService.populateAdminConfig(user, Boolean.TRUE.equals(overwrite));
    return ResponseEntity.ok(populatedObj);
  }

  @Override
  public ResponseEntity<Object> populatePortal(String filePathName, Boolean overwrite) {
    AdminUser user = authUtilService.requireAdminUser(request);
    var populatedObj =
        populateExtService.populatePortal(filePathName, user, Boolean.TRUE.equals(overwrite));
    return ResponseEntity.ok(populatedObj);
  }

  @Override
  public ResponseEntity<Object> populateSiteContent(
      String portalShortcode, String filePathName, Boolean overwrite) {
    AdminUser user = authUtilService.requireAdminUser(request);
    var populatedObj =
        populateExtService.populateSiteContent(
            portalShortcode, filePathName, user, Boolean.TRUE.equals(overwrite));
    return ResponseEntity.ok(populatedObj);
  }

  @Override
  public ResponseEntity<Object> populateSurvey(
      String portalShortcode, String filePathName, Boolean overwrite) {
    AdminUser user = authUtilService.requireAdminUser(request);
    var populatedObj =
        populateExtService.populateSurvey(
            portalShortcode, filePathName, user, Boolean.TRUE.equals(overwrite));
    return ResponseEntity.ok(populatedObj);
  }

  @Override
  public ResponseEntity<Object> populateEnrollee(
      String portalShortcode,
      String envName,
      String studyShortcode,
      String filePathName,
      Boolean overwrite) {
    EnvironmentName environmentName = EnvironmentName.valueOf(envName);
    AdminUser user = authUtilService.requireAdminUser(request);
    var populatedObj =
        populateExtService.populateEnrollee(
            portalShortcode,
            environmentName,
            studyShortcode,
            filePathName,
            user,
            Boolean.TRUE.equals(overwrite));
    return ResponseEntity.ok(populatedObj);
  }

  @Override
  public ResponseEntity<Void> bulkPopulateEnrollees(
      String portalShortcode, String envName, String studyShortcode, Integer numEnrollees) {
    EnvironmentName environmentName = EnvironmentName.valueOf(envName);
    AdminUser user = authUtilService.requireAdminUser(request);
    populateExtService.bulkPopulateEnrollees(
        portalShortcode, environmentName, studyShortcode, numEnrollees, user);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<Resource> exportPortal(String portalShortcode) {
    AdminUser user = authUtilService.requireAdminUser(request);
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      populateExtService.exportPortal(portalShortcode, baos, user);
      return ResponseEntity.ok().body(new ByteArrayResource(baos.toByteArray()));
    } catch (IOException e) {
      throw new InternalServerErrorException("Error exporting portal", e);
    }
  }
}
