package bio.terra.javatemplate.iam;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import bio.terra.javatemplate.BaseSpringBootTest;
import org.broadinstitute.dsde.workbench.client.sam.ApiException;
import org.broadinstitute.dsde.workbench.client.sam.api.StatusApi;
import org.broadinstitute.dsde.workbench.client.sam.model.SystemStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

@ExtendWith(MockitoExtension.class)
class SamServiceTest extends BaseSpringBootTest {

  @MockBean private SamClient samClient;
  @MockBean private StatusApi statusApi;

  @Autowired private SamService samService;

  private void mockStatus() {
    when(samClient.statusApi()).thenReturn(statusApi);
  }

  @Test
  void status() throws Exception {
    mockStatus();
    SystemStatus status = new SystemStatus().ok(true);
    when(statusApi.getSystemStatus()).thenReturn(status);
    var samStatus = samService.status();
    assertTrue(samStatus.isOk());
  }

  @Test
  void statusDown() throws Exception {
    mockStatus();
    SystemStatus status = new SystemStatus().ok(false);
    when(statusApi.getSystemStatus()).thenReturn(status);
    var samStatus = samService.status();
    assertFalse(samStatus.isOk());
  }

  @Test
  void statusException() throws Exception {
    mockStatus();
    when(statusApi.getSystemStatus()).thenThrow(new ApiException());
    var samStatus = samService.status();
    assertFalse(samStatus.isOk());
  }
}
