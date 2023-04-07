package bio.terra.pearl.api.admin.controller;

import bio.terra.pearl.api.admin.api.DatasetsApi;
import bio.terra.pearl.core.service.datarepo.DataRepoExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class DatasetApiController implements DatasetsApi {

  private DataRepoExportService dataRepoExportService;

  @Autowired
  public DatasetApiController(DataRepoExportService dataRepoExportService) {
    this.dataRepoExportService = dataRepoExportService;
  }

  @Override
  public ResponseEntity<Object> listDatasets() {
    return new ResponseEntity<>(dataRepoExportService.listDatasets(), HttpStatus.OK);
  }
}
