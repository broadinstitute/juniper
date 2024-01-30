package bio.terra.pearl.api.participant.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import bio.terra.pearl.api.participant.config.StatusCheckConfiguration;
import bio.terra.pearl.api.participant.model.SystemStatus;
import bio.terra.pearl.api.participant.model.SystemStatusSystems;
import java.util.Map;
import org.junit.jupiter.api.Test;

class BaseStatusServiceTest {

  @Test
  void getCurrentStatus() {
      StatusCheckConfiguration config = new StatusCheckConfiguration(true, 0, 0, 10);
    BaseStatusService service = new BaseStatusService(config);
      SystemStatusSystems status = new SystemStatusSystems().ok(true);
    service.registerStatusCheck("test", () -> status);
    assertThat(service.getCurrentStatus(), is(new SystemStatus().ok(false)));
    service.checkStatus();
    assertThat(
        service.getCurrentStatus(),
        is(new SystemStatus().ok(true).systems(Map.of("test", status))));
  }
}
