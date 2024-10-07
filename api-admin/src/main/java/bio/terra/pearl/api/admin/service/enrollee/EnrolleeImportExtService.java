package bio.terra.pearl.api.admin.service.enrollee;

import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.auth.EnforcePortalStudyEnvPermission;
import bio.terra.pearl.api.admin.service.auth.context.PortalStudyEnvAuthContext;
import bio.terra.pearl.core.model.dataimport.Import;
import bio.terra.pearl.core.model.dataimport.ImportItemStatus;
import bio.terra.pearl.core.model.dataimport.ImportStatus;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import bio.terra.pearl.core.service.export.EnrolleeImportService;
import bio.terra.pearl.core.service.export.dataimport.ImportFileFormat;
import bio.terra.pearl.core.service.export.dataimport.ImportItemService;
import bio.terra.pearl.core.service.export.dataimport.ImportService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class EnrolleeImportExtService {
  private EnrolleeImportService enrolleImportService;
  private StudyEnvironmentService studyEnvironmentService;
  private AuthUtilService authUtilService;
  private ImportService importService;
  private ImportItemService importItemService;

  public EnrolleeImportExtService(
      EnrolleeImportService enrolleImportService,
      AuthUtilService authUtilService,
      ImportService importService,
      ImportItemService importItemService,
      StudyEnvironmentService studyEnvironmentService) {
    this.enrolleImportService = enrolleImportService;
    this.authUtilService = authUtilService;
    this.importService = importService;
    this.importItemService = importItemService;
    this.studyEnvironmentService = studyEnvironmentService;
  }

  @EnforcePortalStudyEnvPermission(permission = "participant_data_view")
  public List<Import> list(PortalStudyEnvAuthContext authContext) {
    return importService.findByStudyEnvWithItems(authContext.getStudyEnvironment().getId());
  }

  @EnforcePortalStudyEnvPermission(permission = "participant_data_view")
  public Import get(PortalStudyEnvAuthContext authContext, UUID id) {
    Import dataImport =
        importService.find(id).orElseThrow(() -> new NotFoundException("Import not found"));
    assertImportInStudyEnv(authContext, dataImport);
    importItemService.attachImportItems(dataImport);
    return dataImport;
  }

  @EnforcePortalStudyEnvPermission(permission = "participant_data_edit")
  public Import importData(
      PortalStudyEnvAuthContext authContext, InputStream tsvData, ImportFileFormat fileFormat) {
    return enrolleImportService.importEnrollees(
        authContext.getPortalShortcode(),
        authContext.getStudyShortcode(),
        authContext.getStudyEnvironment(),
        tsvData,
        authContext.getOperator().getId(),
        fileFormat);
  }

  @Transactional
  @EnforcePortalStudyEnvPermission(permission = "participant_data_edit")
  public void delete(PortalStudyEnvAuthContext authContext, UUID id) {
    Import dataImport =
        importService.find(id).orElseThrow(() -> new NotFoundException("Import not found"));
    assertImportInStudyEnv(authContext, dataImport);
    importService.deleteEnrolleesByImportId(id);
    importService.updateStatus(id, ImportStatus.DELETED);
  }

  @Transactional
  @EnforcePortalStudyEnvPermission(permission = "participant_data_edit")
  public void deleteImportItem(PortalStudyEnvAuthContext authContext, UUID importId, UUID id) {
    Import dataImport =
        importService.find(importId).orElseThrow(() -> new NotFoundException("Import not found"));
    assertImportInStudyEnv(authContext, dataImport);
    importItemService.find(id).orElseThrow(() -> new NotFoundException("ImportItem not found"));
    importItemService.deleteEnrolleeByItemId(id);
    importItemService.updateStatus(id, ImportItemStatus.DELETED);
  }

  private void assertImportInStudyEnv(PortalStudyEnvAuthContext authContext, Import dataImport) {
    if (!dataImport.getStudyEnvironmentId().equals(authContext.getStudyEnvironment().getId())) {
      throw new PermissionDeniedException(
          "Import Id does not belong to the given study environment");
    }
  }
}
