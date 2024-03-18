package bio.terra.pearl.api.admin.controller.internal;

import bio.terra.common.exception.InternalServerErrorException;
import bio.terra.pearl.api.admin.api.PopulateApi;
import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.api.admin.service.PopulateExtService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.site.SiteContent;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.populate.service.AdminConfigPopulator;
import bio.terra.pearl.populate.service.BaseSeedPopulator;
import jakarta.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;

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
    BaseSeedPopulator.SetupStats populatedObj = populateExtService.populateBaseSeed(user);
    return ResponseEntity.ok(populatedObj);
  }

  @Override
  public ResponseEntity<Object> populateAdminConfig(Boolean overwrite) {
    AdminUser user = authUtilService.requireAdminUser(request);
    AdminConfigPopulator.AdminConfigStats populatedObj =
        populateExtService.populateAdminConfig(user, Boolean.TRUE.equals(overwrite));
    return ResponseEntity.ok(populatedObj);
  }

  @Override
  public ResponseEntity<Object> populatePortal(
      String filePathName, Boolean overwrite, String shortcodeOverride) {
    AdminUser user = authUtilService.requireAdminUser(request);
    if (StringUtils.isBlank(shortcodeOverride)) {
      shortcodeOverride = null;
    }
    Portal populatedObj =
        populateExtService.populatePortal(
            filePathName, user, Boolean.TRUE.equals(overwrite), shortcodeOverride);
    return ResponseEntity.ok(populatedObj);
  }

  @Override
  public ResponseEntity<Object> uploadPortal(
      Boolean overwrite, String shortcodeOverride, MultipartFile portalZip) {
    AdminUser user = authUtilService.requireAdminUser(request);
    if (StringUtils.isBlank(shortcodeOverride)) {
      shortcodeOverride = null;
    }
    Portal populatedObj =
        populateExtService.populatePortal(
            portalZip, user, Boolean.TRUE.equals(overwrite), shortcodeOverride);
    return ResponseEntity.ok(populatedObj);
  }

  @Override
  public ResponseEntity<Object> populateSiteContent(
      String portalShortcode, String filePathName, Boolean overwrite) {
    AdminUser user = authUtilService.requireAdminUser(request);
    SiteContent populatedObj =
        populateExtService.populateSiteContent(
            portalShortcode, filePathName, user, Boolean.TRUE.equals(overwrite));
    return ResponseEntity.ok(populatedObj);
  }

  @Override
  public ResponseEntity<Object> populateSurvey(
      String portalShortcode, String filePathName, Boolean overwrite) {
    AdminUser user = authUtilService.requireAdminUser(request);
    Survey populatedObj =
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
    Enrollee populatedObj =
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
  public ResponseEntity<Resource> extractPortal(String portalShortcode) {
    AdminUser user = authUtilService.requireAdminUser(request);
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      populateExtService.extractPortal(portalShortcode, baos, user);
      return ResponseEntity.ok().body(new ByteArrayResource(baos.toByteArray()));
    } catch (IOException e) {
      throw new InternalServerErrorException("Error exporting portal", e);
    }
  }
}
