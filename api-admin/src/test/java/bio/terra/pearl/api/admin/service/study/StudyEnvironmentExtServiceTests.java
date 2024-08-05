package bio.terra.pearl.api.admin.service.study;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import bio.terra.pearl.api.admin.AuthAnnotationSpec;
import bio.terra.pearl.api.admin.AuthTestUtils;
import bio.terra.pearl.api.admin.BaseSpringBootTest;
import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.auth.SandboxOnly;
import bio.terra.pearl.core.model.kit.KitType;
import jakarta.ws.rs.BadRequestException;
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

  @Test
  public void testValidateCanAddKitTypes() {
    List<String> updatedKitTypes = List.of("KitType1", "KitType2");
    List<KitType> existingKitTypes =
        List.of(
            KitType.builder().name("KitType1").build(), KitType.builder().name("KitType2").build());

    assertDoesNotThrow(
        () ->
            studyEnvironmentExtService.validateNoRemovalOfKitTypes(
                updatedKitTypes, existingKitTypes));
  }

  @Test
  public void testValidateCantRemoveKitTypes() {
    List<String> updatedKitTypes = List.of("KitType1");
    List<KitType> existingKitTypes =
        List.of(
            KitType.builder().name("KitType1").build(), KitType.builder().name("KitType2").build());

    assertThrows(
        BadRequestException.class,
        () ->
            studyEnvironmentExtService.validateNoRemovalOfKitTypes(
                updatedKitTypes, existingKitTypes),
        "You may not remove a kit type from a study environment");
  }

  @Test
  public void testInvalidKitTypeErrors() {
    List<String> updatedKitTypes = List.of("KitType1", "InvalidKitType");
    List<KitType> allowedKitTypes =
        List.of(
            KitType.builder().name("KitType1").build(), KitType.builder().name("KitType2").build());

    assertThrows(
        BadRequestException.class,
        () -> studyEnvironmentExtService.validateKitTypes(updatedKitTypes, allowedKitTypes),
        "Invalid kit type");
  }
}
