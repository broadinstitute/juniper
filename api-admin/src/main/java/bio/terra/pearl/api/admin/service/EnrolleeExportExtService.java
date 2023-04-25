package bio.terra.pearl.api.admin.service;

import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.export.EnrolleeExportService;
import bio.terra.pearl.core.service.export.instance.ExportOptions;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import java.io.OutputStream;
import org.springframework.stereotype.Service;

@Service
public class EnrolleeExportExtService {
  private AuthUtilService authUtilService;
  private StudyEnvironmentService studyEnvironmentService;
  private EnrolleeExportService enrolleeExportService;

  public EnrolleeExportExtService(
      AuthUtilService authUtilService,
      StudyEnvironmentService studyEnvironmentService,
      EnrolleeExportService enrolleeExportService) {
    this.authUtilService = authUtilService;
    this.studyEnvironmentService = studyEnvironmentService;
    this.enrolleeExportService = enrolleeExportService;
  }

  public void export(
      ExportOptions options,
      String portalShortcode,
      String studyShortcode,
      EnvironmentName environmentName,
      OutputStream os,
      AdminUser user) {
    Portal portal = authUtilService.authUserToPortal(user, portalShortcode);
    authUtilService.authUserToStudy(user, portalShortcode, studyShortcode);
    StudyEnvironment studyEnv =
        studyEnvironmentService.findByStudy(studyShortcode, environmentName).get();
    try {
      enrolleeExportService.export(options, portal.getId(), studyEnv.getId(), os);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
