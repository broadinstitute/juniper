package bio.terra.pearl.api.admin.service.enrollee;

import bio.terra.pearl.api.admin.AuthAnnotationSpec;
import bio.terra.pearl.api.admin.AuthTestUtils;
import bio.terra.pearl.api.admin.BaseSpringBootTest;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class WithdrawnEnrolleeExtServiceTests extends BaseSpringBootTest {
  @Autowired private WithdrawnEnrolleeExtService withdrawnEnrolleeExtService;

  @Test
  public void testMethodAnnotations() {
    AuthTestUtils.assertAllMethodsAnnotated(
        withdrawnEnrolleeExtService,
        Map.of("getAll", AuthAnnotationSpec.withPortalStudyEnvPerm("participant_data_view")));
  }
}
