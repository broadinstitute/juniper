package bio.terra.pearl.api.admin.service.enrollee;

import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.dataimport.Import;
import bio.terra.pearl.core.model.dataimport.ImportItem;
import bio.terra.pearl.core.model.dataimport.ImportItemStatus;
import bio.terra.pearl.core.model.dataimport.ImportStatus;
import bio.terra.pearl.core.model.study.PortalStudy;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.dataimport.ImportItemService;
import bio.terra.pearl.core.service.dataimport.ImportService;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import bio.terra.pearl.core.service.export.EnrolleeImportService;
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

  public List<Import> list(
      String portalShortcode, String studyShortcode, EnvironmentName envName, AdminUser operator) {
    authUtilService.authUserToStudy(operator, portalShortcode, studyShortcode);
    StudyEnvironment studyEnv =
        studyEnvironmentService
            .findByStudy(studyShortcode, envName)
            .orElseThrow(() -> new NotFoundException("Study environment not found"));
    return importService.findByStudyEnvWithItems(studyEnv.getId());
  }

  public Import get(
      String portalShortcode,
      String studyShortcode,
      EnvironmentName envName,
      AdminUser operator,
      UUID id) {
    authUtilService.authUserToStudy(operator, portalShortcode, studyShortcode);
    StudyEnvironment studyEnv =
        studyEnvironmentService
            .findByStudy(studyShortcode, envName)
            .orElseThrow(() -> new NotFoundException("Study environment not found"));
    Import dataImport =
        importService.find(id).orElseThrow(() -> new NotFoundException("Import not found"));
    importItemService.attachImportItems(dataImport);
    return dataImport;
  }

  public Import importData(
      String portalShortcode,
      String studyShortcode,
      EnvironmentName environmentName,
      InputStream tsvData,
      AdminUser operator) {
    authUtilService.authUserToPortal(operator, portalShortcode);
    PortalStudy portalStudy =
        authUtilService.authUserToStudy(operator, portalShortcode, studyShortcode);
    StudyEnvironment studyEnv =
        studyEnvironmentService.verifyStudy(studyShortcode, environmentName);
    return enrolleImportService.importEnrollees(
        portalShortcode, studyShortcode, studyEnv, tsvData, operator.getId());
  }

  @Transactional
  public void delete(
      String portalShortcode,
      String studyShortcode,
      EnvironmentName environmentName,
      UUID id,
      AdminUser operator) {
    authUtilService.authUserToPortal(operator, portalShortcode);
    PortalStudy portalStudy =
        authUtilService.authUserToStudy(operator, portalShortcode, studyShortcode);
    StudyEnvironment studyEnv =
        studyEnvironmentService.verifyStudy(studyShortcode, environmentName);
    Import dataImport =
        importService.find(id).orElseThrow(() -> new NotFoundException("Import not found"));
    if (!dataImport.getStudyEnvironmentId().equals(studyEnv.getId())) {
      throw new PermissionDeniedException(
          "Import Id does not belong to the given study environment");
    }
    importService.deleteEnrolleesByImportId(id);
    importService.updateStatus(id, ImportStatus.DELETED);
  }

  @Transactional
  public void deleteImportItem(
      String portalShortcode,
      String studyShortcode,
      EnvironmentName environmentName,
      UUID importId,
      UUID id,
      AdminUser operator) {
    authUtilService.authUserToPortal(operator, portalShortcode);
    PortalStudy portalStudy =
        authUtilService.authUserToStudy(operator, portalShortcode, studyShortcode);
    StudyEnvironment studyEnv =
        studyEnvironmentService.verifyStudy(studyShortcode, environmentName);
    Import dataImport =
        importService.find(importId).orElseThrow(() -> new NotFoundException("Import not found"));
    if (!dataImport.getStudyEnvironmentId().equals(studyEnv.getId())) {
      throw new PermissionDeniedException(
          "Import Id does not belong to the given study environment");
    }
    ImportItem importItem =
        importItemService.find(id).orElseThrow(() -> new NotFoundException("ImportItem not found"));
    importItemService.deleteEnrolleeById(id);
    importItemService.updateStatus(id, ImportItemStatus.DELETED);
  }
}
