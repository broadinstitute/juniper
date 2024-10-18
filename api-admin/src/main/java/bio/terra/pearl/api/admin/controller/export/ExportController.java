package bio.terra.pearl.api.admin.controller.export;

import bio.terra.pearl.api.admin.api.ExportApi;
import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.auth.context.PortalStudyEnvAuthContext;
import bio.terra.pearl.api.admin.service.export.EnrolleeExportExtService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.service.export.ExportFileFormat;
import bio.terra.pearl.core.service.export.ExportOptionsWithExpression;
import bio.terra.pearl.core.service.search.EnrolleeSearchExpression;
import bio.terra.pearl.core.service.search.EnrolleeSearchExpressionParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class ExportController implements ExportApi {
  private final EnrolleeSearchExpressionParser enrolleeSearchExpressionParser;
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
      HttpServletResponse response,
      EnrolleeSearchExpressionParser enrolleeSearchExpressionParser) {
    this.authUtilService = authUtilService;
    this.request = request;
    this.enrolleeExportExtService = enrolleeExportExtService;
    this.objectMapper = objectMapper;
    this.response = response;
    this.enrolleeSearchExpressionParser = enrolleeSearchExpressionParser;
  }

  /** just gets the export as a row TSV string, with no accompanying data dictionary */
  @Override
  public ResponseEntity<Resource> exportData(
      String portalShortcode, String studyShortcode, String envName, Object body) {
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    AdminUser user = authUtilService.requireAdminUser(request);

    ExportOptionsWithExpression exportOptions =
        objectMapper.convertValue(body, ExportOptionsWithExpression.class);
    exportOptions.setFilterExpression(
        !StringUtils.isBlank(exportOptions.getFilterString())
            ? enrolleeSearchExpressionParser.parseRule(exportOptions.getFilterString())
            : null);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    enrolleeExportExtService.export(
        PortalStudyEnvAuthContext.of(user, portalShortcode, studyShortcode, environmentName),
        exportOptions,
        baos);
    return ResponseEntity.ok().body(new ByteArrayResource(baos.toByteArray()));
  }

  /** gets a data dictionary for the environment */
  @Override
  public ResponseEntity<Resource> exportDictionary(
      String portalShortcode, String studyShortcode, String envName, Object body) {
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    AdminUser user = authUtilService.requireAdminUser(request);
    ExportOptionsWithExpression exportOptions =
        objectMapper.convertValue(body, ExportOptionsWithExpression.class);
    exportOptions.setFilterExpression(
        !StringUtils.isBlank(exportOptions.getFilterString())
            ? enrolleeSearchExpressionParser.parseRule(exportOptions.getFilterString())
            : null);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    enrolleeExportExtService.exportDictionary(
        PortalStudyEnvAuthContext.of(user, portalShortcode, studyShortcode, environmentName),
        exportOptions,
        baos);
    return ResponseEntity.ok().body(new ByteArrayResource(baos.toByteArray()));
  }

  private ExportOptionsWithExpression optionsFromParams(
      String filter,
      String fileFormat,
      Integer limit,
      Boolean splitOptionsIntoColumns,
      Boolean stableIdsForOptions,
      Boolean includeOnlyMostRecent,
      Boolean includeSubHeaders,
      List<String> excludeModules) {
    EnrolleeSearchExpression searchExp =
        !StringUtils.isBlank(filter) ? enrolleeSearchExpressionParser.parseRule(filter) : null;

    ExportOptionsWithExpression exportOptions =
        ExportOptionsWithExpression.builder()
            .splitOptionsIntoColumns(
                splitOptionsIntoColumns != null ? splitOptionsIntoColumns : false)
            .stableIdsForOptions(stableIdsForOptions != null ? stableIdsForOptions : false)
            .onlyIncludeMostRecent(includeOnlyMostRecent != null ? includeOnlyMostRecent : false)
            .filterString(filter)
            .filterExpression(searchExp)
            .fileFormat(
                fileFormat != null ? ExportFileFormat.valueOf(fileFormat) : ExportFileFormat.TSV)
            .rowLimit(limit)
            .includeSubHeaders(includeSubHeaders)
            .excludeModules(excludeModules != null ? excludeModules : List.of())
            .build();
    return exportOptions;
  }
}
