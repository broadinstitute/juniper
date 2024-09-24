package bio.terra.pearl.api.admin.controller.dataimport;

import bio.terra.pearl.api.admin.api.DataImportApi;
import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.auth.context.PortalStudyEnvAuthContext;
import bio.terra.pearl.api.admin.service.enrollee.EnrolleeImportExtService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.service.export.dataimport.ImportFileFormat;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class DataImportController implements DataImportApi {
  private AuthUtilService authUtilService;
  private HttpServletRequest request;
  private EnrolleeImportExtService enrolleeImportExtService;

  public DataImportController(
      AuthUtilService authUtilService,
      HttpServletRequest request,
      EnrolleeImportExtService enrolleeImportExtService) {

    this.authUtilService = authUtilService;
    this.request = request;
    this.enrolleeImportExtService = enrolleeImportExtService;
  }

  @Override
  public ResponseEntity<Object> getAll(
      String portalShortcode, String studyShortcode, String envName) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    return ResponseEntity.ok(
        enrolleeImportExtService.list(
            PortalStudyEnvAuthContext.of(
                operator, portalShortcode, studyShortcode, environmentName)));
  }

  @Override
  public ResponseEntity<Object> get(
      String portalShortcode, String studyShortcode, String envName, UUID importId) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    return ResponseEntity.ok(
        enrolleeImportExtService.get(
            PortalStudyEnvAuthContext.of(
                operator, portalShortcode, studyShortcode, environmentName),
            importId));
  }

  @Override
  public ResponseEntity<Object> importData(
      String portalShortcode, String studyShortcode, String envName, MultipartFile importFile) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    ImportFileFormat fileFormat = ImportFileFormat.TSV;
    if (importFile.getOriginalFilename().toLowerCase().contains(".csv")) {
      fileFormat = ImportFileFormat.CSV;
    }
    try {
      return ResponseEntity.ok(
          enrolleeImportExtService.importData(
              PortalStudyEnvAuthContext.of(
                  operator, portalShortcode, studyShortcode, environmentName),
              importFile.getInputStream(),
              fileFormat));
    } catch (IOException e) {
      throw new IllegalArgumentException("could not read import data");
    }
  }

  @Override
  public ResponseEntity<Void> delete(
      String portalShortcode, String studyShortcode, String envName, UUID importId) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    enrolleeImportExtService.delete(
        PortalStudyEnvAuthContext.of(operator, portalShortcode, studyShortcode, environmentName),
        importId);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<Void> deleteItem(
      String portalShortcode,
      String studyShortcode,
      String envName,
      UUID importId,
      UUID importItemId) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    enrolleeImportExtService.deleteImportItem(
        PortalStudyEnvAuthContext.of(operator, portalShortcode, studyShortcode, environmentName),
        importId,
        importItemId);
    return ResponseEntity.noContent().build();
  }
}
