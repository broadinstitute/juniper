package bio.terra.pearl.api.admin.controller.export;

import bio.terra.pearl.api.admin.api.ExportApi;
import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.api.admin.service.EnrolleeExportExtService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.service.export.instance.ExportOptions;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class ExportController implements ExportApi {
  private AuthUtilService authUtilService;
  private HttpServletRequest request;
  private EnrolleeExportExtService enrolleeExportExtService;
  private ObjectMapper objectMapper;

  public ExportController(
      AuthUtilService authUtilService,
      HttpServletRequest request,
      EnrolleeExportExtService enrolleeExportExtService,
      ObjectMapper objectMapper) {
    this.authUtilService = authUtilService;
    this.request = request;
    this.enrolleeExportExtService = enrolleeExportExtService;
    this.objectMapper = objectMapper;
  }

  /** just gets the export as a row TSV string, with no accompanying data dictionary */
  @Override
  public ResponseEntity<Object> exportData(
      String portalShortcode, String studyShortcode, String envName, Object body) {
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    AdminUser user = authUtilService.requireAdminUser(request);

    ExportOptions exportOptions = new ExportOptions();
    if (body != null) {
      exportOptions = objectMapper.convertValue(body, ExportOptions.class);
    }

    String export =
        enrolleeExportExtService.exportAsString(
            exportOptions, portalShortcode, studyShortcode, environmentName, user);
    return ResponseEntity.ok(export);
  }
}
