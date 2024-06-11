package bio.terra.pearl.api.admin.service;

import bio.terra.pearl.api.admin.model.CreateDataset;
import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.datarepo.DataRepoJob;
import bio.terra.pearl.core.model.datarepo.Dataset;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.datarepo.DataRepoExportService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DataRepoExportExtService {

  private final AuthUtilService authUtilService;
  private final DataRepoExportService dataRepoExportService;
  private final StudyEnvironmentService studyEnvironmentService;

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
    authUtilService.authUserToPortalWithPermission(user, portalShortcode, "tdr_export");
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
    authUtilService.authUserToPortalWithPermission(user, portalShortcode, "tdr_export");
    authUtilService.authUserToStudy(user, portalShortcode, studyShortcode);

    Dataset dataset = dataRepoExportService.getDatasetByName(datasetName);

    return dataRepoExportService.getJobHistoryForDataset(dataset.getId());
  }

  public void createDataset(
      String portalShortcode,
      String studyShortcode,
      EnvironmentName environmentName,
      CreateDataset createDataset,
      AdminUser user) {
    authUtilService.authUserToPortalWithPermission(user, portalShortcode, "tdr_export");
    authUtilService.authUserToStudy(user, portalShortcode, studyShortcode);

    StudyEnvironment studyEnv =
        studyEnvironmentService.findByStudy(studyShortcode, environmentName).get();

    dataRepoExportService.createDataset(
        studyEnv, createDataset.getName(), createDataset.getDescription(), user);
  }

  public void deleteDataset(
      String portalShortcode,
      String studyShortcode,
      EnvironmentName environmentName,
      String datasetName,
      AdminUser user) {
    authUtilService.authUserToPortalWithPermission(user, portalShortcode, "tdr_export");
    authUtilService.authUserToStudy(user, portalShortcode, studyShortcode);

    StudyEnvironment studyEnv =
        studyEnvironmentService.findByStudy(studyShortcode, environmentName).get();

    dataRepoExportService.deleteDataset(studyEnv, datasetName);
  }
}
