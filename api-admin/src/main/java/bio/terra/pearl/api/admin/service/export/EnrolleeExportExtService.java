package bio.terra.pearl.api.admin.service.export;

import bio.terra.pearl.api.admin.service.auth.EnforcePortalStudyEnvPermission;
import bio.terra.pearl.api.admin.service.auth.context.PortalStudyEnvAuthContext;
import bio.terra.pearl.core.service.export.DictionaryExportService;
import bio.terra.pearl.core.service.export.EnrolleeExportService;
import bio.terra.pearl.core.service.export.ExportOptions;
import bio.terra.pearl.core.service.export.ExportOptionsParsed;
import java.io.OutputStream;
import org.springframework.stereotype.Service;

@Service
public class EnrolleeExportExtService {
  private EnrolleeExportService enrolleeExportService;
  private DictionaryExportService dictionaryExportService;

  public EnrolleeExportExtService(
      EnrolleeExportService enrolleeExportService,
      DictionaryExportService dictionaryExportService) {
    this.enrolleeExportService = enrolleeExportService;
    this.dictionaryExportService = dictionaryExportService;
  }

  @EnforcePortalStudyEnvPermission(permission = "participant_data_view")
  public void export(
      PortalStudyEnvAuthContext authContext, ExportOptionsParsed options, OutputStream os) {
    enrolleeExportService.export(options, authContext.getStudyEnvironment().getId(), os);
  }

  @EnforcePortalStudyEnvPermission(permission = "BASE")
  public void exportDictionary(
      PortalStudyEnvAuthContext authContext, ExportOptions exportOptions, OutputStream os) {
    dictionaryExportService.exportDictionary(
        exportOptions,
        authContext.getPortal().getId(),
        authContext.getStudyEnvironment().getId(),
        os);
  }
}
