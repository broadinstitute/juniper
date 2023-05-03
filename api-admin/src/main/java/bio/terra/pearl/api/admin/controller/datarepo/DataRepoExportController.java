package bio.terra.pearl.api.admin.controller.datarepo;

import bio.terra.pearl.api.admin.api.DatarepoApi;
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
  public ResponseEntity<Object> getDatasetsForStudyEnvironment(
      String portalShortcode, String studyShortcode, String envName) {
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    AdminUser user = authUtilService.requireAdminUser(request);

    List<Dataset> datasets =
        dataRepoExportExtService.getDatasetsForStudyEnvironment(
            portalShortcode, studyShortcode, environmentName, user);

    return ResponseEntity.ok().body(datasets);
  }

  @Override
  public ResponseEntity<Object> getJobHistoryForDataset(
      String portalShortcode, String studyShortcode, String envName, String datasetName) {
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    AdminUser user = authUtilService.requireAdminUser(request);

    List<DataRepoJob> jobHistory =
        dataRepoExportExtService.getJobHistoryForDataset(
            portalShortcode, studyShortcode, environmentName, datasetName, user);

    return ResponseEntity.ok().body(jobHistory);
  }

  @Override
  public ResponseEntity<Object> createDatasetForStudyEnvironment(
      String portalShortcode, String studyShortcode, String envName) {
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    AdminUser user = authUtilService.requireAdminUser(request);

    String jobId =
        dataRepoExportExtService.createDataset(
            portalShortcode, studyShortcode, environmentName, user);

    return ResponseEntity.ok().body(jobId);
  }
}
