package bio.terra.pearl.api.admin.service.study;

import bio.terra.pearl.api.admin.AuthAnnotationSpec;
import bio.terra.pearl.api.admin.AuthTestUtils;
import bio.terra.pearl.api.admin.BaseSpringBootTest;
import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.auth.SandboxOnly;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class StudyEnvironmentExtServiceTests extends BaseSpringBootTest {
  @Autowired private StudyEnvironmentExtService studyEnvironmentExtService;

  @Test
  public void assertAllMethods() {
    AuthTestUtils.assertAllMethodsAnnotated(
        studyEnvironmentExtService,
        Map.of(
            "update",
                AuthAnnotationSpec.withPortalStudyEnvPerm(
                    "survey_edit", List.of(SandboxOnly.class)),
            "updateConfig", AuthAnnotationSpec.withPortalStudyEnvPerm("study_settings_edit"),
            "updateKitTypes", AuthAnnotationSpec.withPortalStudyEnvPerm("study_settings_edit"),
            "getKitTypes",
                AuthAnnotationSpec.withPortalStudyEnvPerm(AuthUtilService.BASE_PERMISSON),
            "getAllowedKitTypes",
                AuthAnnotationSpec.withPortalStudyEnvPerm(AuthUtilService.BASE_PERMISSON),
            "getStats", AuthAnnotationSpec.withPortalStudyEnvPerm(AuthUtilService.BASE_PERMISSON)));
  }
}
