package bio.terra.pearl.api.admin.service;

import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import bio.terra.pearl.populate.service.BaseSeedPopulator;
import bio.terra.pearl.populate.service.EnrolleePopulator;
import bio.terra.pearl.populate.service.PortalPopulator;
import bio.terra.pearl.populate.service.SurveyPopulator;
import bio.terra.pearl.populate.service.contexts.FilePopulateContext;
import bio.terra.pearl.populate.service.contexts.PortalPopulateContext;
import bio.terra.pearl.populate.service.contexts.StudyPopulateContext;
import java.io.IOException;
import org.springframework.stereotype.Service;

@Service
public class PopulateExtService {
  private BaseSeedPopulator baseSeedPopulator;
  private EnrolleePopulator enrolleePopulator;
  private SurveyPopulator surveyPopulator;
  private PortalPopulator portalPopulator;

  public PopulateExtService(
      BaseSeedPopulator baseSeedPopulator,
      EnrolleePopulator enrolleePopulator,
      SurveyPopulator surveyPopulator,
      PortalPopulator portalPopulator) {
    this.baseSeedPopulator = baseSeedPopulator;
    this.enrolleePopulator = enrolleePopulator;
    this.surveyPopulator = surveyPopulator;
    this.portalPopulator = portalPopulator;
  }

  public BaseSeedPopulator.SetupStats populateBaseSeed(AdminUser user) {
    authorizeUser(user);
    try {
      return baseSeedPopulator.populate(new FilePopulateContext(""));
    } catch (IOException e) {
      throw new IllegalArgumentException("populate failed", e);
    }
  }

  public Portal populatePortal(String filePathName, AdminUser user) {
    authorizeUser(user);
    try {
      return portalPopulator.populate(new FilePopulateContext(filePathName));
    } catch (IOException e) {
      throw new IllegalArgumentException("populate failed", e);
    }
  }

  public Survey populateSurvey(String portalShortcode, String filePathName, AdminUser user) {
    authorizeUser(user);
    PortalPopulateContext config = new PortalPopulateContext(filePathName, portalShortcode, null);
    try {
      return surveyPopulator.populate(config);
    } catch (IOException e) {
      throw new IllegalArgumentException("populate failed", e);
    }
  }

  public Enrollee populateEnrollee(
      String portalShortcode,
      EnvironmentName envName,
      String studyShortcode,
      String filePathName,
      AdminUser user) {
    authorizeUser(user);
    StudyPopulateContext config =
        new StudyPopulateContext(filePathName, portalShortcode, studyShortcode, envName);
    try {
      return enrolleePopulator.populate(config);
    } catch (IOException e) {
      throw new IllegalArgumentException("populate failed", e);
    }
  }

  protected void authorizeUser(AdminUser user) {
    if (user.isSuperuser()) {
      return;
    }
    throw new PermissionDeniedException("You do not have access");
  }
}
