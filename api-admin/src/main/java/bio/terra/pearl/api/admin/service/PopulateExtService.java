package bio.terra.pearl.api.admin.service;

import bio.terra.pearl.api.admin.service.auth.EnforcePortalStudyEnvPermission;
import bio.terra.pearl.api.admin.service.auth.SuperuserOnly;
import bio.terra.pearl.api.admin.service.auth.context.OperatorAuthContext;
import bio.terra.pearl.api.admin.service.auth.context.PortalStudyEnvAuthContext;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.site.SiteContent;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.populate.service.*;
import bio.terra.pearl.populate.service.contexts.FilePopulateContext;
import bio.terra.pearl.populate.service.contexts.PortalPopulateContext;
import bio.terra.pearl.populate.service.contexts.StudyPopulateContext;
import bio.terra.pearl.populate.service.extract.PortalExtractService;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipInputStream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PopulateExtService {
  private final BaseSeedPopulator baseSeedPopulator;
  private final EnrolleePopulator enrolleePopulator;
  private final SurveyPopulator surveyPopulator;
  private final PortalPopulator portalPopulator;
  private final SiteContentPopulator siteContentPopulator;
  private final PortalParticipantUserPopulator portalParticipantUserPopulator;
  private final AdminConfigPopulator adminConfigPopulator;
  private final PortalExtractService portalExtractService;

  public PopulateExtService(
      BaseSeedPopulator baseSeedPopulator,
      EnrolleePopulator enrolleePopulator,
      SurveyPopulator surveyPopulator,
      PortalPopulator portalPopulator,
      SiteContentPopulator siteContentPopulator,
      PortalParticipantUserPopulator portalParticipantUserPopulator,
      AdminConfigPopulator adminConfigPopulator,
      PortalExtractService portalExtractService) {
    this.baseSeedPopulator = baseSeedPopulator;
    this.enrolleePopulator = enrolleePopulator;
    this.surveyPopulator = surveyPopulator;
    this.portalPopulator = portalPopulator;
    this.siteContentPopulator = siteContentPopulator;
    this.portalParticipantUserPopulator = portalParticipantUserPopulator;
    this.adminConfigPopulator = adminConfigPopulator;
    this.portalExtractService = portalExtractService;
  }

  @SuperuserOnly
  public BaseSeedPopulator.SetupStats populateBaseSeed(OperatorAuthContext authContext) {
    try {
      return baseSeedPopulator.populate("");
    } catch (IOException e) {
      throw new IllegalArgumentException("populate failed", e);
    }
  }

  @SuperuserOnly
  public AdminConfigPopulator.AdminConfigStats populateAdminConfig(
      OperatorAuthContext authContext, boolean overwrite) {
    try {
      return adminConfigPopulator.populate(overwrite);
    } catch (IOException e) {
      throw new IllegalArgumentException("populate failed", e);
    }
  }

  @SuperuserOnly
  public Portal populatePortal(
      OperatorAuthContext authContext,
      MultipartFile zipFile,
      boolean overwrite,
      String shortcodeOverride) {
    try {
      ZipInputStream zis = new ZipInputStream(zipFile.getInputStream());
      return portalPopulator.populateFromZipFile(zis, overwrite, shortcodeOverride);
    } catch (IOException e) {
      throw new IllegalArgumentException("error reading/writing zip file", e);
    }
  }

  @SuperuserOnly
  public Portal populatePortal(
      OperatorAuthContext authContext,
      String filePathName,
      boolean overwrite,
      String shortcodeOverride) {
    return portalPopulator.populate(
        new FilePopulateContext(filePathName, false, shortcodeOverride), overwrite);
  }

  @SuperuserOnly
  public Survey populateSurvey(
      OperatorAuthContext authContext,
      String portalShortcode,
      String filePathName,
      boolean overwrite) {
    PortalPopulateContext config =
        new PortalPopulateContext(
            filePathName, portalShortcode, null, new HashMap<>(), false, null);
    return surveyPopulator.populate(config, overwrite);
  }

  @SuperuserOnly
  public SiteContent populateSiteContent(
      OperatorAuthContext authContext,
      String portalShortcode,
      String filePathName,
      boolean overwrite) {
    PortalPopulateContext config =
        new PortalPopulateContext(
            filePathName, portalShortcode, null, new HashMap<>(), false, null);
    try {
      // first, repopulate images to cove any new/changed images.
      String portalFilePath = "portals/%s/portal.json".formatted(portalShortcode);
      portalPopulator.populateImages(portalFilePath, overwrite);
      // then repopulate the sitecontent itself
      return siteContentPopulator.populate(config, overwrite);
    } catch (IOException e) {
      throw new IllegalArgumentException("populate failed", e);
    }
  }

  @EnforcePortalStudyEnvPermission(permission = "BASE")
  public Enrollee populateEnrollee(
      PortalStudyEnvAuthContext authContext, String filePathName, boolean overwrite) {
    StudyPopulateContext config =
        new StudyPopulateContext(
            filePathName,
            authContext.getPortalShortcode(),
            authContext.getStudyShortcode(),
            authContext.getEnvironmentName(),
            new HashMap<>(),
            false,
            null);
    return enrolleePopulator.populate(config, overwrite);
  }

  @EnforcePortalStudyEnvPermission(permission = "BASE")
  public Enrollee populateEnrollee(
      PortalStudyEnvAuthContext authContext,
      EnrolleePopulateType enrolleePopulateType,
      String username) {
    StudyPopulateContext config =
        new StudyPopulateContext(
            null,
            authContext.getPortalShortcode(),
            authContext.getStudyShortcode(),
            authContext.getEnvironmentName(),
            new HashMap<>(),
            false,
            null);
    return enrolleePopulator.populateFromType(enrolleePopulateType, username, config);
  }

  @SuperuserOnly
  public void bulkPopulateEnrollees(
      OperatorAuthContext authContext,
      String portalShortcode,
      EnvironmentName envName,
      String studyShortcode,
      Integer numEnrollees) {
    List<String> usernamesToLink =
        portalParticipantUserPopulator.bulkPopulateParticipants(
            portalShortcode, envName, studyShortcode, numEnrollees);
    enrolleePopulator.bulkPopulateEnrollees(
        portalShortcode, envName, studyShortcode, usernamesToLink);
  }

  @SuperuserOnly
  public void extractPortal(
      OperatorAuthContext authContext, String portalShortcode, OutputStream os) throws IOException {
    portalExtractService.extract(portalShortcode, os);
  }

  @Transactional
  @SuperuserOnly
  public Object populateCommand(
      OperatorAuthContext authContext, String command, Object commandParams) {
    if ("CONVERT_CONSENTS".equals(command)) {
      throw new IllegalArgumentException("that command is no longer supported");
    }
    throw new IllegalArgumentException("unknown command");
  }
}
