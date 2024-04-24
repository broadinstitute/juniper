package bio.terra.pearl.api.admin.service.enrollee;

import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.dataimport.Import;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.dataimport.ImportService;
import bio.terra.pearl.core.service.export.EnrolleeImportService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class EnrolleeImportExtService {
  private EnrolleeImportService enrolleImportService;
  private StudyEnvironmentService studyEnvironmentService;
  private AuthUtilService authUtilService;
  private ImportService importService;

  public void EnrolleImportExtService(
      EnrolleeImportService enrolleImportService,
      AuthUtilService authUtilService,
      ImportService importService,
      StudyEnvironmentService studyEnvironmentService) {
    this.enrolleImportService = enrolleImportService;
    this.authUtilService = authUtilService;
    this.importService = importService;
    this.studyEnvironmentService = studyEnvironmentService;
  }

  public List<Import> list(String portalShortcode, AdminUser operator) {
    authUtilService.authUserToPortal(operator, portalShortcode);
    return importService.findAll();
  }

  public Import importData(
      String portalShortcode,
      String studyShortcode,
      String envName,
      InputStream tsvData,
      AdminUser operator) {
    authUtilService.authUserToPortal(operator, portalShortcode);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    StudyEnvironment studyEnv =
        studyEnvironmentService.verifyStudy(studyShortcode, environmentName);
    return enrolleImportService.importEnrollees(
        portalShortcode, studyShortcode, studyEnv, tsvData, operator.getId());
  }

  public void delete(String portalShortcode, UUID id, AdminUser operator) {
    authUtilService.authUserToPortal(operator, portalShortcode);
    importService.delete(id, CascadeProperty.EMPTY_SET);
  }
}
