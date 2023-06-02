package bio.terra.pearl.api.admin.service;

import bio.terra.pearl.api.admin.MockAuthServiceAlwaysRejects;
import bio.terra.pearl.api.admin.model.CreateDataset;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DataRepoExportExtServiceTests {

  private DataRepoExportExtService emptyService =
      new DataRepoExportExtService(new MockAuthServiceAlwaysRejects(), null, null);

  @Test
  public void listDatasetsForStudyEnvironmentRequiresAuth() {
    AdminUser user = new AdminUser();
    Assertions.assertThrows(
        PermissionDeniedException.class,
        () ->
            emptyService.listDatasetsForStudyEnvironment(
                "someportal", "somestudy", EnvironmentName.sandbox, user));
  }

  @Test
  public void getJobHistoryForDatasetRequiresAuth() {
    AdminUser user = new AdminUser();
    Assertions.assertThrows(
        PermissionDeniedException.class,
        () ->
            emptyService.getJobHistoryForDataset(
                "someportal", "somestudy", EnvironmentName.sandbox, "somedataset", user));
  }

  @Test
  public void createDatasetRequiresSuperUser() {
    AdminUser user = new AdminUser();
    Assertions.assertThrows(
        PermissionDeniedException.class,
        () ->
            emptyService.createDataset(
                "someportal",
                "somestudy",
                EnvironmentName.sandbox,
                new CreateDataset().name("somedataset").description("a dataset"),
                user));
  }

  @Test
  public void deleteDatasetRequiresSuperUser() {
    AdminUser user = new AdminUser();
    Assertions.assertThrows(
        PermissionDeniedException.class,
        () ->
            emptyService.deleteDataset(
                "someportal", "somestudy", EnvironmentName.sandbox, "somedataset", user));
  }
}
