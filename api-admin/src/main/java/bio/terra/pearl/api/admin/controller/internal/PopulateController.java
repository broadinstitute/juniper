package bio.terra.pearl.api.admin.controller.internal;

import bio.terra.pearl.api.admin.api.PopulateApi;
import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.api.admin.service.PopulateExtService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import javax.servlet.http.HttpServletRequest;
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
}
