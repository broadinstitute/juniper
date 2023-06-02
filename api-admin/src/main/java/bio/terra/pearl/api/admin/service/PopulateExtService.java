package bio.terra.pearl.api.admin.service;

import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import bio.terra.pearl.populate.service.*;
import bio.terra.pearl.populate.service.contexts.FilePopulateContext;
import bio.terra.pearl.populate.service.contexts.PortalPopulateContext;
import bio.terra.pearl.populate.service.contexts.StudyPopulateContext;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PopulateExtService {
  private BaseSeedPopulator baseSeedPopulator;
  private EnrolleePopulator enrolleePopulator;
  private SurveyPopulator surveyPopulator;
  private PortalPopulator portalPopulator;
  private PortalParticipantUserPopulator portalParticipantUserPopulator;
  private AdminConfigPopulator adminConfigPopulator;

  public PopulateExtService(
      BaseSeedPopulator baseSeedPopulator,
      EnrolleePopulator enrolleePopulator,
      SurveyPopulator surveyPopulator,
      PortalPopulator portalPopulator,
      PortalParticipantUserPopulator portalParticipantUserPopulator,
      AdminConfigPopulator adminConfigPopulator) {
    this.baseSeedPopulator = baseSeedPopulator;
    this.enrolleePopulator = enrolleePopulator;
    this.surveyPopulator = surveyPopulator;
    this.portalPopulator = portalPopulator;
    this.portalParticipantUserPopulator = portalParticipantUserPopulator;
    this.adminConfigPopulator = adminConfigPopulator;
  }

  public BaseSeedPopulator.SetupStats populateBaseSeed(AdminUser user) {
    authorizeUser(user);
    try {
      return baseSeedPopulator.populate();
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

  public Portal populatePortal(String filePathName, AdminUser user, boolean overwrite) {
    authorizeUser(user);
    try {
      return portalPopulator.populate(new FilePopulateContext(filePathName), overwrite);
    } catch (IOException e) {
      throw new IllegalArgumentException("populate failed", e);
    }
  }

  public Survey populateSurvey(
      String portalShortcode, String filePathName, AdminUser user, boolean overwrite) {
    authorizeUser(user);
    PortalPopulateContext config =
        new PortalPopulateContext(filePathName, portalShortcode, null, new HashMap<>());
    try {
      return surveyPopulator.populate(config, overwrite);
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
            filePathName, portalShortcode, studyShortcode, envName, new HashMap<>());
    try {
      return enrolleePopulator.populate(config, overwrite);
    } catch (IOException e) {
      throw new IllegalArgumentException("populate failed", e);
    }
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

  protected void authorizeUser(AdminUser user) {
    if (user.isSuperuser()) {
      return;
    }
    throw new PermissionDeniedException("You do not have access");
  }
}
