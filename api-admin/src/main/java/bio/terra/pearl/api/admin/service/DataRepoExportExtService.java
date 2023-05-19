package bio.terra.pearl.api.admin.service;

import bio.terra.pearl.api.admin.model.CreateDataset;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.datarepo.DataRepoJob;
import bio.terra.pearl.core.model.datarepo.Dataset;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.datarepo.DataRepoExportService;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DataRepoExportExtService {

  private AuthUtilService authUtilService;
  private DataRepoExportService dataRepoExportService;
  private StudyEnvironmentService studyEnvironmentService;

  public DataRepoExportExtService(
      AuthUtilService authUtilService,
      DataRepoExportService dataRepoExportService,
      StudyEnvironmentService studyEnvironmentService) {
    this.authUtilService = authUtilService;
    this.dataRepoExportService = dataRepoExportService;
    this.studyEnvironmentService = studyEnvironmentService;
  }

  public List<Dataset> listDatasetsForStudyEnvironment(
      String portalShortcode,
      String studyShortcode,
      EnvironmentName environmentName,
      AdminUser user) {
    Portal portal = authUtilService.authUserToPortal(user, portalShortcode);
    authUtilService.authUserToStudy(user, portalShortcode, studyShortcode);

    StudyEnvironment studyEnv =
        studyEnvironmentService.findByStudy(studyShortcode, environmentName).get();

    return dataRepoExportService.listDatasetsForStudyEnvironment(studyEnv.getId());
  }

  public List<DataRepoJob> getJobHistoryForDataset(
      String portalShortcode,
      String studyShortcode,
      EnvironmentName environmentName,
      String datasetName,
      AdminUser user) {
    Portal portal = authUtilService.authUserToPortal(user, portalShortcode);
    authUtilService.authUserToStudy(user, portalShortcode, studyShortcode);

    StudyEnvironment studyEnv =
        studyEnvironmentService.findByStudy(studyShortcode, environmentName).get();

    return dataRepoExportService.getJobHistoryForDataset(studyEnv.getId(), datasetName);
  }

  public void createDataset(
      String portalShortcode,
      String studyShortcode,
      EnvironmentName environmentName,
      CreateDataset createDataset,
      AdminUser user) {
    if (!user.isSuperuser()) {
      throw new PermissionDeniedException("You do not have permissions to perform this operation");
    }
    Portal portal = authUtilService.authUserToPortal(user, portalShortcode);
    authUtilService.authUserToStudy(user, portalShortcode, studyShortcode);

    StudyEnvironment studyEnv =
        studyEnvironmentService.findByStudy(studyShortcode, environmentName).get();

    dataRepoExportService.createDataset(
        studyEnv, createDataset.getName(), createDataset.getDescription());
  }
}
