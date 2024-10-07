package bio.terra.pearl.api.admin.controller.datarepo;

import bio.terra.pearl.api.admin.api.DatarepoApi;
import bio.terra.pearl.api.admin.model.CreateDataset;
import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.auth.context.PortalStudyEnvAuthContext;
import bio.terra.pearl.api.admin.service.export.DataRepoExportExtService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.export.datarepo.DataRepoJob;
import bio.terra.pearl.core.model.export.datarepo.Dataset;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class DataRepoExportController implements DatarepoApi {

  private AuthUtilService authUtilService;
  private DataRepoExportExtService dataRepoExportExtService;
  private HttpServletRequest request;

  public DataRepoExportController(
      AuthUtilService authUtilService,
      DataRepoExportExtService dataRepoExportExtService,
      HttpServletRequest request) {
    this.authUtilService = authUtilService;
    this.dataRepoExportExtService = dataRepoExportExtService;
    this.request = request;
  }

  @Override
  public ResponseEntity<Object> listDatasetsForStudyEnvironment(
      String portalShortcode, String studyShortcode, String envName) {
    AdminUser user = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    PortalStudyEnvAuthContext authContext =
        PortalStudyEnvAuthContext.of(user, portalShortcode, studyShortcode, environmentName);
    List<Dataset> datasets = dataRepoExportExtService.listDatasetsForStudyEnvironment(authContext);
    return ResponseEntity.ok().body(datasets);
  }

  @Override
  public ResponseEntity<Object> getJobHistoryForDataset(
      String portalShortcode, String studyShortcode, String envName, String datasetName) {
    AdminUser user = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    PortalStudyEnvAuthContext authContext =
        PortalStudyEnvAuthContext.of(user, portalShortcode, studyShortcode, environmentName);
    List<DataRepoJob> jobHistory =
        dataRepoExportExtService.getJobHistoryForDataset(authContext, datasetName);

    return ResponseEntity.ok().body(jobHistory);
  }

  @Override
  public ResponseEntity<Void> createDatasetForStudyEnvironment(
      String portalShortcode, String studyShortcode, String envName, CreateDataset createDataset) {
    AdminUser user = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    PortalStudyEnvAuthContext authContext =
        PortalStudyEnvAuthContext.of(user, portalShortcode, studyShortcode, environmentName);
    dataRepoExportExtService.createDataset(authContext, createDataset);

    return ResponseEntity.accepted().build();
  }

  @Override
  public ResponseEntity<Void> deleteDataset(
      String portalShortcode, String studyShortcode, String envName, String datasetName) {
    AdminUser user = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    PortalStudyEnvAuthContext authContext =
        PortalStudyEnvAuthContext.of(user, portalShortcode, studyShortcode, environmentName);
    dataRepoExportExtService.deleteDataset(authContext, datasetName);

    return ResponseEntity.accepted().build();
  }
}
