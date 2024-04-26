package bio.terra.pearl.api.admin.controller.dataimport;

import bio.terra.pearl.api.admin.api.DataImportApi;
import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.api.admin.service.enrollee.EnrolleeImportExtService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
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
  public ResponseEntity<Void> delete(
      String portalShortcode, String studyShortcode, String envName, UUID importId) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    enrolleeImportExtService.delete(
        portalShortcode, studyShortcode, environmentName, importId, operator);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<Object> get(String portalShortcode, String studyShortcode, String envName) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    return ResponseEntity.ok(enrolleeImportExtService.list(portalShortcode, operator));
  }

  @Override
  public ResponseEntity<Object> importData(
      String portalShortcode, String studyShortcode, String envName, MultipartFile importFile) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    try {
      return ResponseEntity.ok(
          enrolleeImportExtService.importData(
              portalShortcode,
              studyShortcode,
              environmentName,
              importFile.getInputStream(),
              operator));
    } catch (IOException e) {
      throw new IllegalArgumentException("could not read import data");
    }
  }
}
