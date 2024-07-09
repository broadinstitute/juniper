package bio.terra.pearl.api.admin.service.family;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import bio.terra.pearl.api.admin.AuthAnnotationSpec;
import bio.terra.pearl.api.admin.AuthTestUtils;
import bio.terra.pearl.api.admin.BaseSpringBootTest;
import bio.terra.pearl.api.admin.service.auth.context.PortalStudyEnvAuthContext;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.admin.AdminUserFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.factory.participant.FamilyFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.Family;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.exception.NotFoundException;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

class FamilyExtServiceTest extends BaseSpringBootTest {

  @Autowired FamilyExtService familyExtService;
  @Autowired StudyEnvironmentFactory studyEnvironmentFactory;
  @Autowired EnrolleeFactory enrolleeFactory;
  @Autowired FamilyFactory familyFactory;
  @Autowired AdminUserFactory adminUserFactory;

  @Test
  public void testAuthentication() {
    AuthTestUtils.assertAllMethodsAnnotated(
        familyExtService,
        Map.of(
            "find",
            AuthAnnotationSpec.withPortalStudyEnvPerm("participant_data_view"),
            "addEnrollee",
            AuthAnnotationSpec.withPortalStudyEnvPerm("participant_data_edit"),
            "removeEnrollee",
            AuthAnnotationSpec.withPortalStudyEnvPerm("participant_data_edit"),
            "updateProband",
            AuthAnnotationSpec.withPortalStudyEnvPerm("participant_data_edit"),
            "listChangeRecords",
            AuthAnnotationSpec.withPortalStudyEnvPerm("participant_data_view"),
            "findAll",
            AuthAnnotationSpec.withPortalStudyEnvPerm("participant_data_view")));
  }

  @Test
  @Transactional
  public void testFindOnlyInCorrectStudyEnv(TestInfo info) {
    AdminUser operator = adminUserFactory.buildPersisted(getTestName(info), true);
    StudyEnvironmentFactory.StudyEnvironmentBundle bundle =
        studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);
    StudyEnvironmentFactory.StudyEnvironmentBundle otherBundle =
        studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);

    StudyEnvironment studyEnv = bundle.getStudyEnv();

    Enrollee enrollee = enrolleeFactory.buildPersisted(getTestName(info), studyEnv, new Profile());

    Family family = familyFactory.buildPersisted(getTestName(info), enrollee);

    assertEquals(
        family.getId(),
        familyExtService
            .find(
                PortalStudyEnvAuthContext.of(
                    operator,
                    bundle.getPortal().getShortcode(),
                    bundle.getStudy().getShortcode(),
                    EnvironmentName.sandbox),
                family.getShortcode())
            .getId());

    assertThrows(
        NotFoundException.class,
        () ->
            familyExtService.find(
                PortalStudyEnvAuthContext.of(
                    operator,
                    otherBundle.getPortal().getShortcode(),
                    otherBundle.getStudy().getShortcode(),
                    EnvironmentName.sandbox),
                family.getShortcode()));
  }

  @Test
  @Transactional
  public void testAddMemberOnlyInCorrectStudyEnv(TestInfo info) {
    AdminUser operator = adminUserFactory.buildPersisted(getTestName(info), true);
    StudyEnvironmentFactory.StudyEnvironmentBundle bundle =
        studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);

    Enrollee enrollee =
        enrolleeFactory.buildPersisted(getTestName(info), bundle.getStudyEnv(), new Profile());
    Family family = familyFactory.buildPersisted(getTestName(info), enrollee);

    Enrollee newMember =
        enrolleeFactory.buildPersisted(getTestName(info), bundle.getStudyEnv(), new Profile());

    // can add a member to the family
    familyExtService.addEnrollee(
        PortalStudyEnvAuthContext.of(
            operator,
            bundle.getPortal().getShortcode(),
            bundle.getStudy().getShortcode(),
            EnvironmentName.sandbox),
        family.getShortcode(),
        newMember.getShortcode(),
        "Just because");

    // can't add a member to the family in a different study environment
    Enrollee wrongStudyEnv = enrolleeFactory.buildPersisted(getTestName(info));
    assertThrows(
        NotFoundException.class,
        () ->
            familyExtService.addEnrollee(
                PortalStudyEnvAuthContext.of(
                    operator,
                    bundle.getPortal().getShortcode(),
                    bundle.getStudy().getShortcode(),
                    EnvironmentName.sandbox),
                family.getShortcode(),
                wrongStudyEnv.getShortcode(),
                "Just because"));
  }
}
