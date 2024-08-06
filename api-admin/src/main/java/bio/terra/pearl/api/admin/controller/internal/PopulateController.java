package bio.terra.pearl.api.admin.controller.internal;

import bio.terra.common.exception.InternalServerErrorException;
import bio.terra.pearl.api.admin.api.PopulateApi;
import bio.terra.pearl.api.admin.service.PopulateExtService;
import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.auth.context.OperatorAuthContext;
import bio.terra.pearl.api.admin.service.auth.context.PortalStudyEnvAuthContext;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.site.SiteContent;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.populate.service.AdminConfigPopulator;
import bio.terra.pearl.populate.service.BaseSeedPopulator;
import bio.terra.pearl.populate.service.EnrolleePopulateType;
import com.fasterxml.jackson.databind.ObjectMapper;
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
  private final PopulateExtService populateExtService;
  private final HttpServletRequest request;
  private final AuthUtilService authUtilService;
  private final ObjectMapper objectMapper;

  public PopulateController(
      PopulateExtService populateExtService,
      HttpServletRequest request,
      AuthUtilService authUtilService,
      ObjectMapper objectMapper) {
    this.populateExtService = populateExtService;
    this.request = request;
    this.authUtilService = authUtilService;
    this.objectMapper = objectMapper;
  }

  @Override
  public ResponseEntity<Object> populateBaseSeed() {
    AdminUser operator = authUtilService.requireAdminUser(request);
    BaseSeedPopulator.SetupStats populatedObj =
        populateExtService.populateBaseSeed(OperatorAuthContext.of(operator));
    return ResponseEntity.ok(populatedObj);
  }

  @Override
  public ResponseEntity<Object> populateAdminConfig(Boolean overwrite) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    AdminConfigPopulator.AdminConfigStats populatedObj =
        populateExtService.populateAdminConfig(
            OperatorAuthContext.of(operator), Boolean.TRUE.equals(overwrite));
    return ResponseEntity.ok(populatedObj);
  }

  @Override
  public ResponseEntity<Object> populatePortal(
      String filePathName, Boolean overwrite, String shortcodeOverride) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    if (StringUtils.isBlank(shortcodeOverride)) {
      shortcodeOverride = null;
    }
    Portal populatedObj =
        populateExtService.populatePortal(
            OperatorAuthContext.of(operator),
            filePathName,
            Boolean.TRUE.equals(overwrite),
            shortcodeOverride);
    return ResponseEntity.ok(populatedObj);
  }

  @Override
  public ResponseEntity<Object> uploadPortal(
      Boolean overwrite, String shortcodeOverride, MultipartFile portalZip) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    if (StringUtils.isBlank(shortcodeOverride)) {
      shortcodeOverride = null;
    }
    Portal populatedObj =
        populateExtService.populatePortal(
            OperatorAuthContext.of(operator),
            portalZip,
            Boolean.TRUE.equals(overwrite),
            shortcodeOverride);
    return ResponseEntity.ok(populatedObj);
  }

  @Override
  public ResponseEntity<Object> populateSiteContent(
      String portalShortcode, String filePathName, Boolean overwrite) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    SiteContent populatedObj =
        populateExtService.populateSiteContent(
            OperatorAuthContext.of(operator),
            portalShortcode,
            filePathName,
            Boolean.TRUE.equals(overwrite));
    return ResponseEntity.ok(populatedObj);
  }

  @Override
  public ResponseEntity<Object> populateSurvey(
      String portalShortcode, String filePathName, Boolean overwrite) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    Survey populatedObj =
        populateExtService.populateSurvey(
            OperatorAuthContext.of(operator),
            portalShortcode,
            filePathName,
            Boolean.TRUE.equals(overwrite));
    return ResponseEntity.ok(populatedObj);
  }

  @Override
  public ResponseEntity<Object> populateEnrollee(
      String portalShortcode,
      String studyShortcode,
      String envName,
      String filePathName,
      String popType,
      String username,
      Boolean overwrite) {
    EnvironmentName environmentName = EnvironmentName.valueOf(envName);
    AdminUser operator = authUtilService.requireAdminUser(request);
    Enrollee populatedObj;
    if (StringUtils.isBlank(popType)) {
      populatedObj =
          populateExtService.populateEnrollee(
              PortalStudyEnvAuthContext.of(
                  operator, portalShortcode, studyShortcode, environmentName),
              filePathName,
              Boolean.TRUE.equals(overwrite));

    } else {
      EnrolleePopulateType populateType = EnrolleePopulateType.valueOf(popType.toUpperCase());
      populatedObj =
          populateExtService.populateEnrollee(
              PortalStudyEnvAuthContext.of(
                  operator, portalShortcode, studyShortcode, environmentName),
              populateType,
              username);
    }
    return ResponseEntity.ok(populatedObj);
  }

  @Override
  public ResponseEntity<Object> populateCommand(String command, Object body) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    Object result =
        populateExtService.populateCommand(OperatorAuthContext.of(operator), command, body);
    return ResponseEntity.ok(result);
  }

  @Override
  public ResponseEntity<Void> bulkPopulateEnrollees(
      String portalShortcode, String envName, String studyShortcode, Integer numEnrollees) {
    EnvironmentName environmentName = EnvironmentName.valueOf(envName);
    AdminUser operator = authUtilService.requireAdminUser(request);
    populateExtService.bulkPopulateEnrollees(
        OperatorAuthContext.of(operator),
        portalShortcode,
        environmentName,
        studyShortcode,
        numEnrollees);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<Resource> extractPortal(String portalShortcode) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      populateExtService.extractPortal(OperatorAuthContext.of(operator), portalShortcode, baos);
      return ResponseEntity.ok().body(new ByteArrayResource(baos.toByteArray()));
    } catch (IOException e) {
      throw new InternalServerErrorException("Error exporting portal", e);
    }
  }
}
