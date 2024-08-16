package bio.terra.pearl.api.admin.service;

import bio.terra.pearl.api.admin.AuthAnnotationSpec;
import bio.terra.pearl.api.admin.AuthTestUtils;
import bio.terra.pearl.api.admin.BaseSpringBootTest;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class EnrolleeExportExtServiceTests extends BaseSpringBootTest {
  @Autowired private EnrolleeExportExtService enrolleeExportExtService;

  @Test
  public void testAuthentication() {
    AuthTestUtils.assertAllMethodsAnnotated(
        enrolleeExportExtService,
        Map.of(
            "export",
            AuthAnnotationSpec.withPortalStudyEnvPerm("participant_data_view"),
            "exportDictionary",
            AuthAnnotationSpec.withPortalStudyEnvPerm("BASE")));
  }
}
