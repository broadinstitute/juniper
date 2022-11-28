package bio.terra.pearl.api.admin.controller.internal;

import bio.terra.pearl.api.admin.api.PopulateApi;
import bio.terra.pearl.populate.service.PopulateDispatcher;
import bio.terra.pearl.populate.service.Populator;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.io.IOException;

@Controller
public class PopulateController implements PopulateApi {
  private PopulateDispatcher populateDispatcher;

  public PopulateController(PopulateDispatcher populateDispatcher) {
    this.populateDispatcher = populateDispatcher;
  }

  @Override
  public ResponseEntity<Object> populate(String populateType, String filePathName) {
    Populator populator = populateDispatcher.getPopulator(populateType);
    try {
      return ResponseEntity.ok(populator.populate(filePathName));
    } catch (IOException e) {
      throw new RuntimeException("populate failed", e);
    }
  }
}
