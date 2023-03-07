package bio.terra.pearl.api.admin.controller.internal;

import bio.terra.pearl.api.admin.api.PopulateApi;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.populate.service.*;
import bio.terra.pearl.populate.service.contexts.FilePopulateContext;
import bio.terra.pearl.populate.service.contexts.PortalPopulateContext;
import bio.terra.pearl.populate.service.contexts.StudyPopulateContext;
import java.io.IOException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

/**
 * Note this controller does not explicitly validate the safety of the passed-in filenames. Rather,
 * it relies on FilePopulateService.getInputStream to ensure that only files from within the seed
 * directory are allowed to be read.
 */
@Controller
public class PopulateController implements PopulateApi {
  private BaseSeedPopulator baseSeedPopulator;
  private EnrolleePopulator enrolleePopulator;
  private SurveyPopulator surveyPopulator;
  private PortalPopulator portalPopulator;

  public PopulateController(
      BaseSeedPopulator baseSeedPopulator,
      EnrolleePopulator enrolleePopulator,
      SurveyPopulator surveyPopulator,
      PortalPopulator portalPopulator) {
    this.baseSeedPopulator = baseSeedPopulator;
    this.enrolleePopulator = enrolleePopulator;
    this.surveyPopulator = surveyPopulator;
    this.portalPopulator = portalPopulator;
  }

  @Override
  public ResponseEntity<Object> populateBaseSeed() {
    try {
      return ResponseEntity.ok(baseSeedPopulator.populate(new FilePopulateContext("")));
    } catch (IOException e) {
      throw new IllegalArgumentException("populate failed", e);
    }
  }

  @Override
  public ResponseEntity<Object> populatePortal(String filePathName) {
    try {
      return ResponseEntity.ok(portalPopulator.populate(new FilePopulateContext(filePathName)));
    } catch (IOException e) {
      throw new IllegalArgumentException("populate failed", e);
    }
  }

  @Override
  public ResponseEntity<Object> populateSurvey(String portalShortcode, String filePathName) {
    PortalPopulateContext config = new PortalPopulateContext(filePathName, portalShortcode, null);
    try {
      return ResponseEntity.ok(surveyPopulator.populate(config));
    } catch (IOException e) {
      throw new IllegalArgumentException("populate failed", e);
    }
  }

  @Override
  public ResponseEntity<Object> populateEnrollee(
      String portalShortcode, String envName, String studyShortcode, String filePathName) {
    EnvironmentName environmentName = EnvironmentName.valueOf(envName);
    StudyPopulateContext config =
        new StudyPopulateContext(filePathName, portalShortcode, studyShortcode, environmentName);
    try {
      return ResponseEntity.ok(enrolleePopulator.populate(config));
    } catch (IOException e) {
      throw new IllegalArgumentException("populate failed", e);
    }
  }
}
