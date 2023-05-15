package bio.terra.pearl.api.admin.controller.datarepo;

import bio.terra.pearl.api.admin.api.DatarepoApi;
import bio.terra.pearl.api.admin.model.DatasetName;
import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.api.admin.service.DataRepoExportExtService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.datarepo.DataRepoJob;
import bio.terra.pearl.core.model.datarepo.Dataset;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
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

    List<Dataset> datasets =
        dataRepoExportExtService.listDatasetsForStudyEnvironment(
            portalShortcode, studyShortcode, environmentName, user);

    return ResponseEntity.ok().body(datasets);
  }

  @Override
  public ResponseEntity<Object> getJobHistoryForDataset(
      String portalShortcode, String studyShortcode, String envName, String datasetName) {
    AdminUser user = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);

    List<DataRepoJob> jobHistory =
        dataRepoExportExtService.getJobHistoryForDataset(
            portalShortcode, studyShortcode, environmentName, datasetName, user);

    return ResponseEntity.ok().body(jobHistory);
  }

  @Override
  public ResponseEntity<Void> createDatasetForStudyEnvironment(
      String portalShortcode, String studyShortcode, String envName, DatasetName datasetName) {
    AdminUser user = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);

    dataRepoExportExtService.createDataset(
        portalShortcode, studyShortcode, environmentName, datasetName, user);

    return ResponseEntity.accepted().build();
  }
}
