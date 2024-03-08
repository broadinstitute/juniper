package bio.terra.pearl.core.service.participant;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.factory.portal.PortalEnvironmentFactory;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.EnrolleeRelation;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.participant.RelationshipType;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

class EnrolleeRelationServiceTest extends BaseSpringBootTest {

    @Autowired
    EnrolleeFactory enrolleeFactory;

    @Autowired
    StudyEnvironmentFactory studyEnvironmentFactory;

    @Autowired
    PortalEnvironmentFactory portalEnvironmentFactory;

    @Autowired
    EnrolleeRelationService enrolleeRelationService;

    @Autowired
    ParticipantUserService participantUserService;

    @Test
    @Transactional
    void testIsUserProxyForAnyOf(TestInfo info) {
        PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted(getTestName(info));
        StudyEnvironment studyEnv1 = studyEnvironmentFactory.buildPersisted(portalEnv, getTestName(info));
        StudyEnvironment studyEnv2 = studyEnvironmentFactory.buildPersisted(portalEnv, getTestName(info));

        EnrolleeFactory.EnrolleeBundle proxyBundle = enrolleeFactory.buildWithPortalUser(getTestName(info), portalEnv, studyEnv1);
        PortalParticipantUser proxyPpUser = proxyBundle.portalParticipantUser();
        Enrollee proxyEnrollee = proxyBundle.enrollee();

        EnrolleeFactory.EnrolleeBundle targetBundle = enrolleeFactory.buildWithPortalUser(getTestName(info), portalEnv, studyEnv1);
        Enrollee targetEnrollee1 = targetBundle.enrollee();
        Enrollee targetEnrollee2 = enrolleeFactory.buildPersisted(
                getTestName(info),
                studyEnv2.getId(),
                targetBundle.portalParticipantUser().getParticipantUserId(),
                targetBundle.enrollee().getProfileId());

        Assertions.assertFalse(enrolleeRelationService.isUserProxyForAnyOf(proxyPpUser.getParticipantUserId(),
                List.of(targetEnrollee1.getId(), targetEnrollee2.getId())));

        enrolleeRelationService.create(
                EnrolleeRelation
                        .builder()
                        .enrolleeId(proxyEnrollee.getId())
                        .targetEnrolleeId(targetEnrollee1.getId())
                        .relationshipType(RelationshipType.PROXY)
                        .build(),
                getAuditInfo(info)
        );

        Assertions.assertTrue(enrolleeRelationService.isUserProxyForAnyOf(proxyPpUser.getParticipantUserId(),
                List.of(targetEnrollee1.getId(), targetEnrollee2.getId())));

    }

    @Test
    @Transactional
    void testIsRelationshipValid(TestInfo info){
        PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted(getTestName(info));
        StudyEnvironment studyEnv1 = studyEnvironmentFactory.buildPersisted(portalEnv, getTestName(info));
        EnrolleeFactory.EnrolleeBundle proxyBundle = enrolleeFactory.buildWithPortalUser(getTestName(info), portalEnv, studyEnv1);
        Enrollee proxyEnrollee = proxyBundle.enrollee();
        EnrolleeFactory.EnrolleeBundle targetBundle = enrolleeFactory.buildWithPortalUser(getTestName(info), portalEnv, studyEnv1);
        Enrollee targetEnrollee1 = targetBundle.enrollee();
        EnrolleeRelation enrolleeRelation = enrolleeRelationService.create(
                EnrolleeRelation
                        .builder()
                        .enrolleeId(proxyEnrollee.getId())
                        .targetEnrolleeId(targetEnrollee1.getId())
                        .relationshipType(RelationshipType.PROXY)
                        .beginDate(Instant.now())
                        .endDate(null)
                        .build(),
                getAuditInfo(info)
        );
        Assertions.assertTrue(enrolleeRelationService.isRelationshipValid(enrolleeRelation));
        Instant tomorrow = Instant.now().plusSeconds(86400);
        enrolleeRelation.setEndDate(tomorrow);
        enrolleeRelationService.update(enrolleeRelation, getAuditInfo(info));
        Assertions.assertTrue(enrolleeRelationService.isRelationshipValid(enrolleeRelation));

        Instant yesterday = Instant.now().minusSeconds(86400);
        enrolleeRelation.setEndDate(yesterday);
        enrolleeRelationService.update(enrolleeRelation, getAuditInfo(info));
        Assertions.assertFalse(enrolleeRelationService.isRelationshipValid(enrolleeRelation));
    }

    @Test
    @Transactional
    public void testFindAllValidByTwoProperties(TestInfo info) {
        PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted(getTestName(info));
        StudyEnvironment studyEnv1 = studyEnvironmentFactory.buildPersisted(portalEnv, getTestName(info));
        EnrolleeFactory.EnrolleeBundle proxyBundle = enrolleeFactory.buildWithPortalUser(getTestName(info), portalEnv, studyEnv1);
        Enrollee proxyEnrollee = proxyBundle.enrollee();
        EnrolleeFactory.EnrolleeBundle targetBundle = enrolleeFactory.buildWithPortalUser(getTestName(info), portalEnv, studyEnv1);
        Enrollee targetEnrollee1 = targetBundle.enrollee();
        EnrolleeFactory.EnrolleeBundle targetBundle2 = enrolleeFactory.buildWithPortalUser(getTestName(info), portalEnv, studyEnv1);
        Enrollee targetEnrollee2 = targetBundle2.enrollee();
        Instant yesterday = Instant.now().minusSeconds(86400);
        Instant tomorrow = Instant.now().plusSeconds(86400);
        EnrolleeRelation enrolleeRelationNotValid = enrolleeRelationService.create(
                EnrolleeRelation
                        .builder()
                        .enrolleeId(proxyEnrollee.getId())
                        .targetEnrolleeId(targetEnrollee1.getId())
                        .relationshipType(RelationshipType.PROXY)
                        .beginDate(Instant.now())
                        .endDate(yesterday)
                        .build(),
                getAuditInfo(info)
        );
        EnrolleeRelation enrolleeRelationValid = enrolleeRelationService.create(
                EnrolleeRelation
                        .builder()
                        .enrolleeId(proxyEnrollee.getId())
                        .targetEnrolleeId(targetEnrollee2.getId())
                        .relationshipType(RelationshipType.PROXY)
                        .beginDate(Instant.now())
                        .endDate(tomorrow)
                        .build(),
                getAuditInfo(info)
        );
        List<EnrolleeRelation> validRelations = enrolleeRelationService.findByEnrolleeIdAndRelationType(
                proxyEnrollee.getId(),
                RelationshipType.PROXY
        );
        Assertions.assertEquals(1, validRelations.size());
        EnrolleeRelation validRelation = validRelations.get(0);
        Assertions.assertEquals(enrolleeRelationValid.getTargetEnrolleeId(), validRelation.getTargetEnrolleeId());
        Assertions.assertEquals(enrolleeRelationValid.getEnrolleeId(), validRelation.getEnrolleeId());
        Assertions.assertEquals(enrolleeRelationValid.getRelationshipType(), validRelation.getRelationshipType());
    }

    @Test
    @Transactional
    public void testFindAllValidByOneProperty(TestInfo info) {
        PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted(getTestName(info));
        StudyEnvironment studyEnv1 = studyEnvironmentFactory.buildPersisted(portalEnv, getTestName(info));
        EnrolleeFactory.EnrolleeBundle proxyBundle = enrolleeFactory.buildWithPortalUser(getTestName(info), portalEnv, studyEnv1);
        Enrollee proxyEnrollee = proxyBundle.enrollee();
        EnrolleeFactory.EnrolleeBundle targetBundle = enrolleeFactory.buildWithPortalUser(getTestName(info), portalEnv, studyEnv1);
        Enrollee targetEnrollee1 = targetBundle.enrollee();
        EnrolleeFactory.EnrolleeBundle targetBundle2 = enrolleeFactory.buildWithPortalUser(getTestName(info), portalEnv, studyEnv1);
        Enrollee targetEnrollee2 = targetBundle2.enrollee();
        Instant yesterday = Instant.now().minusSeconds(86400);
        Instant tomorrow = Instant.now().plusSeconds(86400);
        EnrolleeRelation enrolleeRelationNotValid = enrolleeRelationService.create(
                EnrolleeRelation
                        .builder()
                        .enrolleeId(proxyEnrollee.getId())
                        .targetEnrolleeId(targetEnrollee1.getId())
                        .relationshipType(RelationshipType.PROXY)
                        .beginDate(Instant.now())
                        .endDate(yesterday)
                        .build(),
                getAuditInfo(info)
        );
        EnrolleeRelation enrolleeRelationValid = enrolleeRelationService.create(
                EnrolleeRelation
                        .builder()
                        .enrolleeId(proxyEnrollee.getId())
                        .targetEnrolleeId(targetEnrollee2.getId())
                        .relationshipType(RelationshipType.PROXY)
                        .beginDate(Instant.now())
                        .endDate(tomorrow)
                        .build(),
                getAuditInfo(info)
        );

        List<EnrolleeRelation> targetEnrollee2ValidRelations = enrolleeRelationService.findByTargetEnrolleeId(targetEnrollee2.getId());
        Assertions.assertEquals(1, targetEnrollee2ValidRelations.size());
        Assertions.assertEquals(enrolleeRelationValid.getEnrolleeId(), targetEnrollee2ValidRelations.get(0).getEnrolleeId());
        Assertions.assertEquals(enrolleeRelationValid.getTargetEnrolleeId(), targetEnrollee2ValidRelations.get(0).getTargetEnrolleeId());
        Assertions.assertEquals(enrolleeRelationValid.getRelationshipType(), targetEnrollee2ValidRelations.get(0).getRelationshipType());

        List<EnrolleeRelation> targetEnrollee1ValidRelations = enrolleeRelationService.findByTargetEnrolleeId(targetEnrollee1.getId());
        Assertions.assertEquals(0, targetEnrollee1ValidRelations.size());

    }
}
