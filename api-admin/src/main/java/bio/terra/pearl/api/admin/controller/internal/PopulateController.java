package bio.terra.pearl.api.admin.controller.internal;

import bio.terra.pearl.api.admin.api.PopulateApi;
import bio.terra.pearl.populate.service.FilePopulateConfig;
import bio.terra.pearl.populate.service.PopulateDispatcher;
import bio.terra.pearl.populate.service.Populator;
import java.io.IOException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

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

  @Override
  public ResponseEntity<Object> populateSurvey(String portalShortcode, String filePathName) {
    Populator populator = populateDispatcher.getPopulator(PopulateDispatcher.PopulateType.SURVEY);
    FilePopulateConfig config = new FilePopulateConfig(filePathName, portalShortcode);
    try {
      return ResponseEntity.ok(populator.populate(config));
    } catch (IOException e) {
      throw new RuntimeException("populate failed", e);
    }
  }
}
