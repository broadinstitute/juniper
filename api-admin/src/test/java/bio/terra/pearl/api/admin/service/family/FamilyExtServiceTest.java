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
import bio.terra.pearl.core.model.participant.FamilyEnrollee;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.participant.FamilyEnrolleeService;
import java.util.List;
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
  @Autowired FamilyEnrolleeService familyEnrolleeService;

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
            AuthAnnotationSpec.withPortalStudyEnvPerm("participant_data_view"),
            "create",
            AuthAnnotationSpec.withPortalStudyEnvPerm("participant_data_edit"),
            "delete",
            AuthAnnotationSpec.withPortalStudyEnvPerm("participant_data_edit")));
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

    // works with shortcode
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

    // works with id
    assertEquals(
        family.getId(),
        familyExtService
            .find(
                PortalStudyEnvAuthContext.of(
                    operator,
                    bundle.getPortal().getShortcode(),
                    bundle.getStudy().getShortcode(),
                    EnvironmentName.sandbox),
                family.getId().toString())
            .getId());

    // throws with shortcode
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

    // throws with id
    assertThrows(
        NotFoundException.class,
        () ->
            familyExtService.find(
                PortalStudyEnvAuthContext.of(
                    operator,
                    otherBundle.getPortal().getShortcode(),
                    otherBundle.getStudy().getShortcode(),
                    EnvironmentName.sandbox),
                family.getId().toString()));
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

  @Test
  @Transactional
  public void createAlsoAddsProbandToFamily(TestInfo info) {
    AdminUser operator = adminUserFactory.buildPersisted(getTestName(info), true);
    StudyEnvironmentFactory.StudyEnvironmentBundle bundle =
        studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);

    Enrollee enrollee =
        enrolleeFactory.buildPersisted(getTestName(info), bundle.getStudyEnv(), new Profile());

    Family created =
        familyExtService.create(
            PortalStudyEnvAuthContext.of(
                operator,
                bundle.getPortal().getShortcode(),
                bundle.getStudy().getShortcode(),
                EnvironmentName.sandbox),
            Family.builder()
                .probandEnrolleeId(enrollee.getId())
                .studyEnvironmentId(bundle.getStudyEnv().getId())
                .build(),
            "Just because");

    assertEquals(enrollee.getId(), created.getProbandEnrolleeId());

    List<FamilyEnrollee> members = familyEnrolleeService.findByFamilyId(created.getId());

    assertEquals(1, members.size());
    assertEquals(1, created.getMembers().size());
    assertEquals(enrollee.getId(), members.get(0).getEnrolleeId());
    assertEquals(enrollee.getId(), created.getMembers().get(0).getId());
  }
}
