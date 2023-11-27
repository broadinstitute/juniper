package bio.terra.pearl.api.admin.controller.export;

import bio.terra.pearl.api.admin.api.ExportApi;
import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.api.admin.service.EnrolleeExportExtService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.service.export.ExportFileFormat;
import bio.terra.pearl.core.service.export.instance.ExportOptions;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class ExportController implements ExportApi {
  private AuthUtilService authUtilService;
  private HttpServletRequest request;
  private EnrolleeExportExtService enrolleeExportExtService;
  private ObjectMapper objectMapper;
  private HttpServletResponse response;

  public ExportController(
      AuthUtilService authUtilService,
      HttpServletRequest request,
      EnrolleeExportExtService enrolleeExportExtService,
      ObjectMapper objectMapper,
      HttpServletResponse response) {
    this.authUtilService = authUtilService;
    this.request = request;
    this.enrolleeExportExtService = enrolleeExportExtService;
    this.objectMapper = objectMapper;
    this.response = response;
  }

  /** just gets the export as a row TSV string, with no accompanying data dictionary */
  @Override
  public ResponseEntity<Resource> exportData(
      String portalShortcode,
      String studyShortcode,
      String envName,
      Boolean splitOptionsIntoColumns,
      Boolean stableIdsForOptions,
      Boolean includeOnlyMostRecent,
      String fileFormat,
      Integer limit) {
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    AdminUser user = authUtilService.requireAdminUser(request);

    ExportOptions exportOptions =
        new ExportOptions(
            splitOptionsIntoColumns != null ? splitOptionsIntoColumns : false,
            stableIdsForOptions != null ? stableIdsForOptions : false,
            includeOnlyMostRecent != null ? includeOnlyMostRecent : false,
            fileFormat != null ? ExportFileFormat.valueOf(fileFormat) : ExportFileFormat.TSV,
            limit);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    enrolleeExportExtService.export(
        exportOptions, portalShortcode, studyShortcode, environmentName, baos, user);
    return ResponseEntity.ok().body(new ByteArrayResource(baos.toByteArray()));
  }

  /** gets a data dictionary for the environment */
  @Override
  public ResponseEntity<Resource> exportDictionary(
      String portalShortcode,
      String studyShortcode,
      String envName,
      Boolean splitOptionsIntoColumns,
      Boolean stableIdsForOptions,
      Boolean includeOnlyMostRecent,
      String fileFormat) {
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    AdminUser user = authUtilService.requireAdminUser(request);
    ExportOptions exportOptions =
        new ExportOptions(
            splitOptionsIntoColumns != null ? splitOptionsIntoColumns : false,
            stableIdsForOptions != null ? stableIdsForOptions : false,
            includeOnlyMostRecent != null ? includeOnlyMostRecent : false,
            fileFormat != null ? ExportFileFormat.valueOf(fileFormat) : ExportFileFormat.TSV,
            null);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    enrolleeExportExtService.exportDictionary(
        exportOptions, portalShortcode, studyShortcode, environmentName, baos, user);
    return ResponseEntity.ok().body(new ByteArrayResource(baos.toByteArray()));
  }
}
