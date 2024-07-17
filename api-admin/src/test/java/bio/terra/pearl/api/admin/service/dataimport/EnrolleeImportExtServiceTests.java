package bio.terra.pearl.api.admin.service.dataimport;

import bio.terra.pearl.api.admin.AuthAnnotationSpec;
import bio.terra.pearl.api.admin.AuthTestUtils;
import bio.terra.pearl.api.admin.BaseSpringBootTest;
import bio.terra.pearl.api.admin.service.enrollee.EnrolleeImportExtService;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class EnrolleeImportExtServiceTests extends BaseSpringBootTest {
  @Autowired private EnrolleeImportExtService enrolleeImportExtService;

  @Test
  public void testAuthentication() {
    AuthTestUtils.assertAllMethodsAnnotated(
        enrolleeImportExtService,
        Map.of(
            "get",
            AuthAnnotationSpec.withPortalStudyEnvPerm("participant_data_view"),
            "list",
            AuthAnnotationSpec.withPortalStudyEnvPerm("participant_data_view"),
            "delete",
            AuthAnnotationSpec.withPortalStudyEnvPerm("participant_data_edit"),
            "deleteImportItem",
            AuthAnnotationSpec.withPortalStudyEnvPerm("participant_data_edit"),
            "importData",
            AuthAnnotationSpec.withPortalStudyEnvPerm("participant_data_edit")));
  }
}
