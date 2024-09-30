package bio.terra.pearl.api.admin.service.export;

import bio.terra.pearl.api.admin.model.CreateDataset;
import bio.terra.pearl.api.admin.service.auth.*;
import bio.terra.pearl.api.admin.service.auth.context.PortalStudyEnvAuthContext;
import bio.terra.pearl.core.model.export.datarepo.DataRepoJob;
import bio.terra.pearl.core.model.export.datarepo.Dataset;
import bio.terra.pearl.core.service.datarepo.DataRepoExportService;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
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

  @EnforcePortalStudyEnvPermission(permission = "tdr_export")
  public List<Dataset> listDatasetsForStudyEnvironment(PortalStudyEnvAuthContext authContext) {
    return dataRepoExportService.listDatasetsForStudyEnvironment(
        authContext.getStudyEnvironment().getId());
  }

  @EnforcePortalStudyEnvPermission(permission = "tdr_export")
  public List<DataRepoJob> getJobHistoryForDataset(
      PortalStudyEnvAuthContext authContext, String datasetName) {
    Dataset dataset = dataRepoExportService.getDatasetByName(datasetName);
    if (dataset.getStudyEnvironmentId().equals(authContext.getStudyEnvironment().getId())) {
      throw new PermissionDeniedException("User does not have permission to view this dataset");
    }
    return dataRepoExportService.getJobHistoryForDataset(dataset.getId());
  }

  @EnforcePortalStudyEnvPermission(permission = "tdr_export")
  public void createDataset(PortalStudyEnvAuthContext authContext, CreateDataset createDataset) {
    dataRepoExportService.createDataset(
        authContext.getStudyEnvironment(),
        createDataset.getName(),
        createDataset.getDescription(),
        authContext.getOperator());
  }

  @EnforcePortalStudyEnvPermission(permission = "tdr_export")
  public void deleteDataset(PortalStudyEnvAuthContext authContext, String datasetName) {
    Dataset dataset = dataRepoExportService.getDatasetByName(datasetName);
    if (dataset.getStudyEnvironmentId().equals(authContext.getStudyEnvironment().getId())) {
      throw new PermissionDeniedException("User does not have permission to view this dataset");
    }
    dataRepoExportService.deleteDataset(authContext.getStudyEnvironment(), datasetName);
  }
}
