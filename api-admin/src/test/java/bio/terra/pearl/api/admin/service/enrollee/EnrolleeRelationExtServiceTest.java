package bio.terra.pearl.api.admin.service.enrollee;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import bio.terra.pearl.api.admin.AuthAnnotationSpec;
import bio.terra.pearl.api.admin.AuthTestUtils;
import bio.terra.pearl.api.admin.BaseSpringBootTest;
import bio.terra.pearl.api.admin.service.auth.context.PortalStudyEnvAuthContext;
import bio.terra.pearl.core.factory.StudyEnvironmentBundle;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.admin.AdminUserFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeAndProxy;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.factory.participant.FamilyFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.EnrolleeRelation;
import bio.terra.pearl.core.model.participant.Family;
import bio.terra.pearl.core.model.participant.RelationshipType;
import bio.terra.pearl.core.service.exception.NotFoundException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

class EnrolleeRelationExtServiceTest extends BaseSpringBootTest {

  @Autowired private EnrolleeRelationExtService enrolleeRelationExtService;
  @Autowired private StudyEnvironmentFactory studyEnvironmentFactory;
  @Autowired private EnrolleeFactory enrolleeFactory;
  @Autowired private AdminUserFactory adminUserFactory;
  @Autowired private FamilyFactory familyFactory;

  @Test
  public void testAuthentication() {
    AuthTestUtils.assertAllMethodsAnnotated(
        enrolleeRelationExtService,
        Map.of(
            "findRelationsForTargetEnrollee",
                AuthAnnotationSpec.withPortalStudyEnvPerm("participant_data_view"),
            "create", AuthAnnotationSpec.withPortalStudyEnvPerm("participant_data_edit"),
            "delete", AuthAnnotationSpec.withPortalStudyEnvPerm("participant_data_edit")));
  }

  @Test
  @Transactional
  public void testFindRelationsForTargetEnrolleeOnlyInCorrectStudyEnv(TestInfo info) {
    AdminUser operator = adminUserFactory.buildPersisted(getTestName(info), true);
    StudyEnvironmentBundle studyEnvBundle =
        studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);
    EnrolleeAndProxy enrolleeAndProxy =
        enrolleeFactory.buildProxyAndGovernedEnrollee(
            getTestName(info), studyEnvBundle.getPortalEnv(), studyEnvBundle.getStudyEnv());

    StudyEnvironmentBundle otherStudyEnv =
        studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);

    List<EnrolleeRelation> relations =
        enrolleeRelationExtService.findRelationsForTargetEnrollee(
            PortalStudyEnvAuthContext.of(
                operator,
                studyEnvBundle.getPortal().getShortcode(),
                studyEnvBundle.getStudy().getShortcode(),
                EnvironmentName.sandbox),
            enrolleeAndProxy.governedEnrollee().getShortcode());

    assertEquals(1, relations.size());

    assertThrows(
        NotFoundException.class,
        () ->
            enrolleeRelationExtService.findRelationsForTargetEnrollee(
                PortalStudyEnvAuthContext.of(
                    operator,
                    otherStudyEnv.getPortal().getShortcode(),
                    otherStudyEnv.getStudy().getShortcode(),
                    EnvironmentName.sandbox),
                enrolleeAndProxy.governedEnrollee().getShortcode()));

    assertThrows(
        NotFoundException.class,
        () ->
            enrolleeRelationExtService.findRelationsForTargetEnrollee(
                PortalStudyEnvAuthContext.of(
                    operator,
                    studyEnvBundle.getPortal().getShortcode(),
                    studyEnvBundle.getStudy().getShortcode(),
                    EnvironmentName.sandbox),
                "DOESNTEXIST"));
  }

  @Test
  @Transactional
  public void testCannotCreateBetweenDifferentStudyEnvs(TestInfo info) {
    AdminUser operator = adminUserFactory.buildPersisted(getTestName(info), true);
    StudyEnvironmentBundle studyEnvBundle =
        studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);
    Enrollee enrollee1 =
        enrolleeFactory.buildPersisted(getTestName(info), studyEnvBundle.getStudyEnv());
    Enrollee enrollee2 =
        enrolleeFactory.buildPersisted(getTestName(info), studyEnvBundle.getStudyEnv());

    Family family = familyFactory.buildPersisted(getTestName(info), enrollee1);

    Enrollee wrongStudyEnv = enrolleeFactory.buildPersisted(getTestName(info));
    Family wrongStudyFamily = familyFactory.buildPersisted(getTestName(info), wrongStudyEnv);

    // can create within the same study env
    enrolleeRelationExtService.create(
        PortalStudyEnvAuthContext.of(
            operator,
            studyEnvBundle.getPortal().getShortcode(),
            studyEnvBundle.getStudy().getShortcode(),
            EnvironmentName.sandbox),
        EnrolleeRelation.builder()
            .enrolleeId(enrollee1.getId())
            .targetEnrolleeId(enrollee2.getId())
            .relationshipType(RelationshipType.FAMILY)
            .familyId(family.getId())
            .familyRelationship("brother")
            .build(),
        "Just because");

    // cannot create between different study envs
    assertThrows(
        NotFoundException.class,
        () ->
            enrolleeRelationExtService.create(
                PortalStudyEnvAuthContext.of(
                    operator,
                    studyEnvBundle.getPortal().getShortcode(),
                    studyEnvBundle.getStudy().getShortcode(),
                    EnvironmentName.sandbox),
                EnrolleeRelation.builder()
                    .enrolleeId(enrollee1.getId())
                    .targetEnrolleeId(wrongStudyEnv.getId())
                    .relationshipType(RelationshipType.FAMILY)
                    .familyId(family.getId())
                    .familyRelationship("brother")
                    .build(),
                "Just because"));

    // cannot add to a family in a different study env
    assertThrows(
        NotFoundException.class,
        () ->
            enrolleeRelationExtService.create(
                PortalStudyEnvAuthContext.of(
                    operator,
                    studyEnvBundle.getPortal().getShortcode(),
                    studyEnvBundle.getStudy().getShortcode(),
                    EnvironmentName.sandbox),
                EnrolleeRelation.builder()
                    .enrolleeId(enrollee1.getId())
                    .targetEnrolleeId(enrollee2.getId())
                    .relationshipType(RelationshipType.FAMILY)
                    .familyId(wrongStudyFamily.getId())
                    .familyRelationship("brother")
                    .build(),
                "Just because"));
  }
}
