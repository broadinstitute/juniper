package bio.terra.pearl.api.admin.service;

import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.auth.EnforcePortalStudyEnvPermission;
import bio.terra.pearl.api.admin.service.auth.context.PortalStudyEnvAuthContext;
import bio.terra.pearl.core.service.export.DictionaryExportService;
import bio.terra.pearl.core.service.export.EnrolleeExportService;
import bio.terra.pearl.core.service.export.ExportOptions;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import java.io.OutputStream;
import org.springframework.stereotype.Service;

@Service
public class EnrolleeExportExtService {
  private AuthUtilService authUtilService;
  private StudyEnvironmentService studyEnvironmentService;
  private EnrolleeExportService enrolleeExportService;
  private DictionaryExportService dictionaryExportService;

  public EnrolleeExportExtService(
      AuthUtilService authUtilService,
      StudyEnvironmentService studyEnvironmentService,
      EnrolleeExportService enrolleeExportService,
      DictionaryExportService dictionaryExportService) {
    this.authUtilService = authUtilService;
    this.studyEnvironmentService = studyEnvironmentService;
    this.enrolleeExportService = enrolleeExportService;
    this.dictionaryExportService = dictionaryExportService;
  }

  @EnforcePortalStudyEnvPermission(permission = "participant_data_view")
  public void export(
      PortalStudyEnvAuthContext authContext, ExportOptions options, OutputStream os) {
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
