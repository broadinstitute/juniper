package bio.terra.pearl.api.admin.service.workflow;

import bio.terra.pearl.api.admin.AuthAnnotationSpec;
import bio.terra.pearl.api.admin.AuthTestUtils;
import bio.terra.pearl.api.admin.BaseSpringBootTest;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class EventExtServiceTests extends BaseSpringBootTest {
  @Autowired private EventExtService eventExtService;

  @Test
  public void testAuthentication() {
    AuthTestUtils.assertAllMethodsAnnotated(
        eventExtService,
        Map.of(
            "findAllByEnrollee",
            AuthAnnotationSpec.withPortalEnrolleePerm("participant_data_view")));
  }
}
