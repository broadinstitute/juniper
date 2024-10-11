package bio.terra.pearl.core.service.participant;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeAndProxy;
import bio.terra.pearl.core.factory.participant.EnrolleeBundle;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.factory.participant.FamilyFactory;
import bio.terra.pearl.core.factory.portal.PortalEnvironmentFactory;
import bio.terra.pearl.core.model.participant.*;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Autowired
    FamilyFactory familyFactory;

    @Autowired
    FamilyEnrolleeService familyEnrolleeService;

    @Test
    @Transactional
    void testIsUserProxyForAnyOf(TestInfo info) {
        PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted(getTestName(info));
        StudyEnvironment studyEnv1 = studyEnvironmentFactory.buildPersisted(portalEnv, getTestName(info));
        StudyEnvironment studyEnv2 = studyEnvironmentFactory.buildPersisted(portalEnv, getTestName(info));

        EnrolleeBundle proxyBundle = enrolleeFactory.buildWithPortalUser(getTestName(info), portalEnv, studyEnv1);
        PortalParticipantUser proxyPpUser = proxyBundle.portalParticipantUser();
        Enrollee proxyEnrollee = proxyBundle.enrollee();

        EnrolleeBundle targetBundle = enrolleeFactory.buildWithPortalUser(getTestName(info), portalEnv, studyEnv1);
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
        Assertions.assertNotNull(enrolleeRelationService.isUserProxyForEnrollee(proxyPpUser.getParticipantUserId(),
                targetEnrollee1.getShortcode()));
    }

    @Test
    @Transactional
    void testIsRelationshipValid(TestInfo info){
        PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted(getTestName(info));
        StudyEnvironment studyEnv1 = studyEnvironmentFactory.buildPersisted(portalEnv, getTestName(info));
        EnrolleeBundle proxyBundle = enrolleeFactory.buildWithPortalUser(getTestName(info), portalEnv, studyEnv1);
        Enrollee proxyEnrollee = proxyBundle.enrollee();
        EnrolleeBundle targetBundle = enrolleeFactory.buildWithPortalUser(getTestName(info), portalEnv, studyEnv1);
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
        EnrolleeBundle proxyBundle = enrolleeFactory.buildWithPortalUser(getTestName(info), portalEnv, studyEnv1);
        Enrollee proxyEnrollee = proxyBundle.enrollee();
        EnrolleeBundle targetBundle = enrolleeFactory.buildWithPortalUser(getTestName(info), portalEnv, studyEnv1);
        Enrollee targetEnrollee1 = targetBundle.enrollee();
        EnrolleeBundle targetBundle2 = enrolleeFactory.buildWithPortalUser(getTestName(info), portalEnv, studyEnv1);
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
        assertEquals(1, validRelations.size());
        EnrolleeRelation validRelation = validRelations.get(0);
        assertEquals(enrolleeRelationValid.getTargetEnrolleeId(), validRelation.getTargetEnrolleeId());
        assertEquals(enrolleeRelationValid.getEnrolleeId(), validRelation.getEnrolleeId());
        assertEquals(enrolleeRelationValid.getRelationshipType(), validRelation.getRelationshipType());
    }

    @Test
    @Transactional
    public void testFindAllValidByOneProperty(TestInfo info) {
        PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted(getTestName(info));
        StudyEnvironment studyEnv1 = studyEnvironmentFactory.buildPersisted(portalEnv, getTestName(info));
        EnrolleeBundle proxyBundle = enrolleeFactory.buildWithPortalUser(getTestName(info), portalEnv, studyEnv1);
        Enrollee proxyEnrollee = proxyBundle.enrollee();
        EnrolleeBundle targetBundle = enrolleeFactory.buildWithPortalUser(getTestName(info), portalEnv, studyEnv1);
        Enrollee targetEnrollee1 = targetBundle.enrollee();
        EnrolleeBundle targetBundle2 = enrolleeFactory.buildWithPortalUser(getTestName(info), portalEnv, studyEnv1);
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
        assertEquals(1, targetEnrollee2ValidRelations.size());
        assertEquals(enrolleeRelationValid.getEnrolleeId(), targetEnrollee2ValidRelations.get(0).getEnrolleeId());
        assertEquals(enrolleeRelationValid.getTargetEnrolleeId(), targetEnrollee2ValidRelations.get(0).getTargetEnrolleeId());
        assertEquals(enrolleeRelationValid.getRelationshipType(), targetEnrollee2ValidRelations.get(0).getRelationshipType());

        List<EnrolleeRelation> targetEnrollee1ValidRelations = enrolleeRelationService.findByTargetEnrolleeId(targetEnrollee1.getId());
        assertEquals(0, targetEnrollee1ValidRelations.size());

    }

    @Test
    @Transactional
    public void findExclusiveProxiedEnrolleesTest(TestInfo info){
        EnrolleeAndProxy hubResponse = enrolleeFactory.buildProxyAndGovernedEnrollee(getTestName(info), "proxyEmail@test.com");
        Enrollee proxyEnrollee = hubResponse.proxy();
        Enrollee governedEnrollee = hubResponse.governedEnrollee();
        List<Enrollee> targetEnrollees = enrolleeRelationService.findExclusiveProxiesForTargetEnrollee(governedEnrollee.getId());
        assertEquals(1, targetEnrollees.size());
        assertEquals(proxyEnrollee, targetEnrollees.get(0));

        //now test that if proxy enrollee has multiple governed users, it is not returned
        Enrollee governed2 = enrolleeFactory.buildPersisted(getTestName(info));
        enrolleeRelationService.create(
                EnrolleeRelation
                        .builder()
                        .enrolleeId(proxyEnrollee.getId())
                        .targetEnrolleeId(governed2.getId())
                        .relationshipType(RelationshipType.PROXY)
                        .beginDate(Instant.now())
                        .endDate(null)
                        .build(),
                getAuditInfo(info)
        );

        List<Enrollee> targetEnrollees2 = enrolleeRelationService.findExclusiveProxiesForTargetEnrollee(governedEnrollee.getId());
        assertEquals(0, targetEnrollees2.size());

        // create another exclusive proxy, make sure they are returned
        Enrollee proxy2 = enrolleeFactory.buildPersisted(getTestName(info));
        enrolleeRelationService.create(
                EnrolleeRelation
                        .builder()
                        .enrolleeId(proxy2.getId())
                        .targetEnrolleeId(governedEnrollee.getId())
                        .relationshipType(RelationshipType.PROXY)
                        .beginDate(Instant.now())
                        .endDate(null)
                        .build(),
                getAuditInfo(info)
        );


        assertEquals(1,
                enrolleeRelationService.findExclusiveProxiesForTargetEnrollee(governedEnrollee.getId()).size());

    }

    @Test
    @Transactional
    void testFindByTargetEnrolleeIdWithEnrolleesAndFamily(TestInfo info) {
        EnrolleeAndProxy hubResponse = enrolleeFactory.buildProxyAndGovernedEnrollee(getTestName(info), "proxyEmail@test.com");
        Enrollee proxyEnrollee = hubResponse.proxy();
        Enrollee governedEnrollee = hubResponse.governedEnrollee();

        List<EnrolleeRelation> enrolleeRelations = enrolleeRelationService.findByTargetEnrolleeIdWithEnrolleesAndFamily(governedEnrollee.getId());

        assertEquals(1, enrolleeRelations.size());

        EnrolleeRelation relation = enrolleeRelations.get(0);

        assertEquals(proxyEnrollee.getId(), relation.getEnrolleeId());
        assertEquals(governedEnrollee.getId(), relation.getTargetEnrolleeId());
        assertEquals(RelationshipType.PROXY, relation.getRelationshipType());
        assertEquals(proxyEnrollee.getId(), relation.getEnrollee().getId());
        assertEquals(governedEnrollee.getId(), relation.getTargetEnrollee().getId());
        Assertions.assertNotNull(relation.getTargetEnrollee().getProfile());
        Assertions.assertNotNull(relation.getEnrollee().getProfile());
    }

    @Test
    @Transactional
    public void testCreateFamilyRelationship(TestInfo info) {
        StudyEnvironment studyEnvironment = studyEnvironmentFactory.buildPersisted(getTestName(info));
        Enrollee proband = enrolleeFactory.buildPersisted(getTestName(info), studyEnvironment);
        Enrollee member1 = enrolleeFactory.buildPersisted(getTestName(info), studyEnvironment);
        Enrollee member2 = enrolleeFactory.buildPersisted(getTestName(info), studyEnvironment);
        Family family = familyFactory.buildPersisted(getTestName(info), proband);
        familyFactory.linkEnrolleeToFamily(member1, family);
        familyFactory.linkEnrolleeToFamily(member2, family);

        List<EnrolleeRelation> relations = enrolleeRelationService.findRelationsForFamily(family.getId());
        assertEquals(0, relations.size());

        enrolleeRelationService.createFamilyRelationship(
                EnrolleeRelation
                        .builder()
                        .enrolleeId(member1.getId())
                        .targetEnrolleeId(member2.getId())
                        .familyId(family.getId())
                        .familyRelationship("brother")
                        .build(),
                getAuditInfo(info)
        );

        relations = enrolleeRelationService.findRelationsForFamily(family.getId());
        assertEquals(1, relations.size());

        EnrolleeRelation relation = relations.get(0);
        assertEquals(member1.getId(), relation.getEnrolleeId());
        assertEquals(member2.getId(), relation.getTargetEnrolleeId());

    }

    @Test
    @Transactional
    public void testCreateFamilyRelationshipAddsToFamily(TestInfo info) {
        StudyEnvironment studyEnvironment = studyEnvironmentFactory.buildPersisted(getTestName(info));
        Enrollee proband = enrolleeFactory.buildPersisted(getTestName(info), studyEnvironment);
        Family family = familyFactory.buildPersisted(getTestName(info), proband);

        List<EnrolleeRelation> relations = enrolleeRelationService.findRelationsForFamily(family.getId());
        assertEquals(0, relations.size());

        List<FamilyEnrollee> members = familyEnrolleeService.findByFamilyId(family.getId());

        assertEquals(1, members.size());
        assertTrue(members.stream().anyMatch(fe -> fe.getEnrolleeId().equals(proband.getId())));


        Enrollee nonMember1 = enrolleeFactory.buildPersisted(getTestName(info), studyEnvironment);
        Enrollee nonMember2 = enrolleeFactory.buildPersisted(getTestName(info), studyEnvironment);

        enrolleeRelationService.createFamilyRelationship(
                EnrolleeRelation
                        .builder()
                        .enrolleeId(nonMember1.getId())
                        .targetEnrolleeId(nonMember2.getId())
                        .familyId(family.getId())
                        .familyRelationship("brother")
                        .build(),
                getAuditInfo(info)
        );

        relations = enrolleeRelationService.findRelationsForFamily(family.getId());
        assertEquals(1, relations.size());

        EnrolleeRelation relation = relations.get(0);
        assertEquals(nonMember1.getId(), relation.getEnrolleeId());
        assertEquals(nonMember2.getId(), relation.getTargetEnrolleeId());

        members = familyEnrolleeService.findByFamilyId(family.getId());

        assertEquals(3, members.size());
        assertTrue(members.stream().anyMatch(fe -> fe.getEnrolleeId().equals(nonMember1.getId())));
        assertTrue(members.stream().anyMatch(fe -> fe.getEnrolleeId().equals(nonMember2.getId())));
        assertTrue(members.stream().anyMatch(fe -> fe.getEnrolleeId().equals(proband.getId())));
    }
}
