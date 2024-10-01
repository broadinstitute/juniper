package bio.terra.pearl.api.admin.service.export;

import bio.terra.pearl.api.admin.BaseSpringBootTest;
import bio.terra.pearl.api.admin.model.CreateDataset;
import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.auth.context.PortalStudyEnvAuthContext;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.service.exception.NotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

public class DataRepoExportExtServiceTests extends BaseSpringBootTest {
  private PortalStudyEnvAuthContext emptyAuthContext =
      PortalStudyEnvAuthContext.of(
          new AdminUser(), "someportal", "somestudy", EnvironmentName.sandbox);

  @Autowired private DataRepoExportExtService dataRepoExportExtService;
  @MockBean private AuthUtilService authUtilService;

  @Test
  public void listDatasetsForStudyEnvironmentRequiresAuth() {
    Assertions.assertThrows(
        NotFoundException.class,
        () -> dataRepoExportExtService.listDatasetsForStudyEnvironment(emptyAuthContext));
    Mockito.verify(authUtilService)
        .authUserToPortalWithPermission(
            emptyAuthContext.getOperator(), emptyAuthContext.getPortalShortcode(), "tdr_export");
  }

  @Test
  public void getJobHistoryForDatasetRequiresAuth() {
    Assertions.assertThrows(
        NotFoundException.class,
        () -> dataRepoExportExtService.getJobHistoryForDataset(emptyAuthContext, "somedataset"));
    Mockito.verify(authUtilService)
        .authUserToPortalWithPermission(
            emptyAuthContext.getOperator(), emptyAuthContext.getPortalShortcode(), "tdr_export");
  }

  @Test
  public void createDatasetRequiresAuth() {
    Assertions.assertThrows(
        NotFoundException.class,
        () -> dataRepoExportExtService.createDataset(emptyAuthContext, new CreateDataset()));
    Mockito.verify(authUtilService)
        .authUserToPortalWithPermission(
            emptyAuthContext.getOperator(), emptyAuthContext.getPortalShortcode(), "tdr_export");
  }

  @Test
  public void deleteDatasetRequiresSuperUser() {
    Assertions.assertThrows(
        NotFoundException.class,
        () -> dataRepoExportExtService.deleteDataset(emptyAuthContext, "somedataset"));
    Mockito.verify(authUtilService)
        .authUserToPortalWithPermission(
            emptyAuthContext.getOperator(), emptyAuthContext.getPortalShortcode(), "tdr_export");
  }
}
