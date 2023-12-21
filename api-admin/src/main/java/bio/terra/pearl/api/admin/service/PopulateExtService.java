package bio.terra.pearl.api.admin.service;

import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.site.SiteContent;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import bio.terra.pearl.populate.service.*;
import bio.terra.pearl.populate.service.contexts.FilePopulateContext;
import bio.terra.pearl.populate.service.contexts.PortalPopulateContext;
import bio.terra.pearl.populate.service.contexts.StudyPopulateContext;
import bio.terra.pearl.populate.service.extract.PortalExtractService;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
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

  public BaseSeedPopulator.SetupStats populateBaseSeed(AdminUser user) {
    authorizeUser(user);
    try {
      return baseSeedPopulator.populate("");
    } catch (IOException e) {
      throw new IllegalArgumentException("populate failed", e);
    }
  }

  public AdminConfigPopulator.AdminConfigStats populateAdminConfig(
      AdminUser user, boolean overwrite) {
    authorizeUser(user);
    try {
      return adminConfigPopulator.populate(overwrite);
    } catch (IOException e) {
      throw new IllegalArgumentException("populate failed", e);
    }
  }

  public Portal populatePortal(MultipartFile zipFile, AdminUser user, boolean overwrite) {
    authorizeUser(user);
    try {
      String folderName =
          "portal_%s_%s"
              .formatted(
                  ZonedDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE),
                  RandomStringUtils.randomAlphabetic(5));
      String tempDirName = FilePopulateService.TMP_POPULATE_DIR + "/" + folderName;
      File tempDir = new File(tempDirName);
      tempDir.mkdirs();
      ZipUtils.unzipFile(tempDir, zipFile);
      return portalPopulator.populate(
          new FilePopulateContext(folderName + "/portal.json", true), overwrite);
    } catch (IOException e) {
      throw new IllegalArgumentException("populate failed", e);
    }
  }

  public Portal populatePortal(String filePathName, AdminUser user, boolean overwrite) {
    authorizeUser(user);
    return portalPopulator.populate(new FilePopulateContext(filePathName), overwrite);
  }

  public Survey populateSurvey(
      String portalShortcode, String filePathName, AdminUser user, boolean overwrite) {
    authorizeUser(user);
    PortalPopulateContext config =
        new PortalPopulateContext(filePathName, portalShortcode, null, new HashMap<>(), false);
    return surveyPopulator.populate(config, overwrite);
  }

  public SiteContent populateSiteContent(
      String portalShortcode, String filePathName, AdminUser user, boolean overwrite) {
    authorizeUser(user);
    PortalPopulateContext config =
        new PortalPopulateContext(filePathName, portalShortcode, null, new HashMap<>(), false);
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

  public Enrollee populateEnrollee(
      String portalShortcode,
      EnvironmentName envName,
      String studyShortcode,
      String filePathName,
      AdminUser user,
      boolean overwrite) {
    authorizeUser(user);
    StudyPopulateContext config =
        new StudyPopulateContext(
            filePathName, portalShortcode, studyShortcode, envName, new HashMap<>(), false);
    return enrolleePopulator.populate(config, overwrite);
  }

  public void bulkPopulateEnrollees(
      String portalShortcode,
      EnvironmentName envName,
      String studyShortcode,
      Integer numEnrollees,
      AdminUser user) {
    authorizeUser(user);

    List<String> usernamesToLink =
        portalParticipantUserPopulator.bulkPopulateParticipants(
            portalShortcode, envName, studyShortcode, numEnrollees);
    enrolleePopulator.bulkPopulateEnrollees(
        portalShortcode, envName, studyShortcode, usernamesToLink);
  }

  public void extractPortal(String portalShortcode, OutputStream os, AdminUser user)
      throws IOException {
    authorizeUser(user);
    portalExtractService.extract(portalShortcode, os);
  }

  protected void authorizeUser(AdminUser user) {
    if (user.isSuperuser()) {
      return;
    }
    throw new PermissionDeniedException("You do not have access");
  }
}
