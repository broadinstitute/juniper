package bio.terra.pearl.core.service.participant;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.factory.participant.FamilyFactory;
import bio.terra.pearl.core.model.participant.*;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FamilyServiceTest extends BaseSpringBootTest {

    @Autowired
    private FamilyService familyService;

    @Autowired
    private StudyEnvironmentFactory studyEnvironmentFactory;

    @Autowired
    private EnrolleeFactory enrolleeFactory;

    @Autowired
    private FamilyFactory familyFactory;

    @Autowired
    private FamilyEnrolleeService familyEnrolleeService;

    @Autowired
    private EnrolleeRelationService enrolleeRelationService;


    @Test
    @Transactional
    public void testCreateFamilyAddsShortcode(TestInfo info) {
        StudyEnvironment studyEnvironment = studyEnvironmentFactory.buildPersisted(getTestName(info));
        Enrollee proband = enrolleeFactory.buildPersisted(getTestName(info), studyEnvironment);

        Family created = familyService.create(
                Family.builder()
                        .studyEnvironmentId(studyEnvironment.getId())
                        .probandEnrolleeId(proband.getId())
                        .build(),
                getAuditInfo(info));

        assertNotNull(created.getShortcode());
        assertTrue(created.getShortcode().startsWith("F_"));
    }

    @Test
    @Transactional
    public void testAddMember(TestInfo info) {
        StudyEnvironment studyEnvironment = studyEnvironmentFactory.buildPersisted(getTestName(info));
        Enrollee proband = enrolleeFactory.buildPersisted(getTestName(info), studyEnvironment);
        Enrollee newMember = enrolleeFactory.buildPersisted(getTestName(info), studyEnvironment);
        Family family = familyFactory.buildPersisted(getTestName(info), proband);

        familyService.addEnrollee(
                family.getShortcode(),
                newMember.getShortcode(),
                newMember.getStudyEnvironmentId(),
                getAuditInfo(info));


        List<FamilyEnrollee> members = familyEnrolleeService.findByFamilyId(family.getId());
        assertEquals(2, members.size());
        assertTrue(members.stream().anyMatch(fe -> fe.getEnrolleeId().equals(newMember.getId())));

        // can't add again
        assertThrows(IllegalArgumentException.class, () -> {
            familyService.addEnrollee(
                    family.getShortcode(),
                    newMember.getShortcode(),
                    newMember.getStudyEnvironmentId(),
                    getAuditInfo(info));
        });
    }

    @Test
    @Transactional
    public void testCannotAddMemberInDifferentStudyEnv(TestInfo info) {
        StudyEnvironment studyEnvironment = studyEnvironmentFactory.buildPersisted(getTestName(info));
        Enrollee proband = enrolleeFactory.buildPersisted(getTestName(info), studyEnvironment);

        // different study environment
        Enrollee newMember = enrolleeFactory.buildPersisted(getTestName(info));

        Family family = familyFactory.buildPersisted(getTestName(info), proband);

        // throws not found so it doesn't leak information
        assertThrows(NotFoundException.class, () -> {
            familyService.addEnrollee(
                    family.getShortcode(),
                    newMember.getShortcode(),
                    newMember.getStudyEnvironmentId(),
                    getAuditInfo(info));
        });
    }

    @Test
    @Transactional
    public void testRemoveMember(TestInfo info) {
        StudyEnvironment studyEnvironment = studyEnvironmentFactory.buildPersisted(getTestName(info));
        Enrollee proband = enrolleeFactory.buildPersisted(getTestName(info), studyEnvironment);
        Family family = familyFactory.buildPersisted(getTestName(info), proband);

        Enrollee newMember = enrolleeFactory.buildPersisted(getTestName(info), studyEnvironment);
        familyFactory.linkEnrolleeToFamily(newMember, family);

        List<FamilyEnrollee> members = familyEnrolleeService.findByFamilyId(family.getId());
        assertEquals(2, members.size());

        familyService.removeEnrollee(
                family.getShortcode(),
                newMember.getShortcode(),
                studyEnvironment.getId(),
                getAuditInfo(info));

        members = familyEnrolleeService.findByFamilyId(family.getId());
        assertEquals(1, members.size());
        assertFalse(members.stream().anyMatch(fe -> fe.getEnrolleeId().equals(newMember.getId())));

        // can't remove again
        assertThrows(NotFoundException.class, () -> {
            familyService.removeEnrollee(
                    family.getShortcode(),
                    newMember.getShortcode(),
                    studyEnvironment.getId(),
                    getAuditInfo(info));
        });
    }

    @Test
    @Transactional
    public void testCannotRemoveProband(TestInfo info) {

        StudyEnvironment studyEnvironment = studyEnvironmentFactory.buildPersisted(getTestName(info));
        Enrollee proband = enrolleeFactory.buildPersisted(getTestName(info), studyEnvironment);
        Family family = familyFactory.buildPersisted(getTestName(info), proband);

        Enrollee newMember = enrolleeFactory.buildPersisted(getTestName(info), studyEnvironment);
        familyFactory.linkEnrolleeToFamily(newMember, family);

        List<FamilyEnrollee> members = familyEnrolleeService.findByFamilyId(family.getId());
        assertEquals(2, members.size());

        assertThrows(IllegalArgumentException.class, () -> {
            familyService.removeEnrollee(
                    family.getShortcode(),
                    proband.getShortcode(),
                    studyEnvironment.getId(),
                    getAuditInfo(info));
        });

    }

    @Test
    @Transactional
    public void testRemoveMemberDeletesRelationships(TestInfo info) {
        StudyEnvironment studyEnvironment = studyEnvironmentFactory.buildPersisted(getTestName(info));
        Enrollee proband = enrolleeFactory.buildPersisted(getTestName(info), studyEnvironment);
        Family family = familyFactory.buildPersisted(getTestName(info), proband);

        Enrollee member1 = enrolleeFactory.buildPersisted(getTestName(info), studyEnvironment);
        Enrollee member2 = enrolleeFactory.buildPersisted(getTestName(info), studyEnvironment);
        familyFactory.linkEnrolleeToFamily(member1, family);
        familyFactory.linkEnrolleeToFamily(member2, family);

        List<FamilyEnrollee> members = familyEnrolleeService.findByFamilyId(family.getId());
        assertEquals(3, members.size());

        // create a relationship between where member1 is source
        enrolleeRelationService.create(
                EnrolleeRelation
                        .builder()
                        .enrolleeId(member1.getId())
                        .targetEnrolleeId(member2.getId())
                        .relationshipType(RelationshipType.FAMILY)
                        .familyId(family.getId())
                        .familyRelationship("brother")
                        .build(),
                getAuditInfo(info));
        // create a relationship between where member1 is target
        enrolleeRelationService.create(
                EnrolleeRelation
                        .builder()
                        .enrolleeId(proband.getId())
                        .targetEnrolleeId(member1.getId())
                        .relationshipType(RelationshipType.FAMILY)
                        .familyId(family.getId())
                        .familyRelationship("father")
                        .build(),
                getAuditInfo(info));
        // create a relationship without member1
        enrolleeRelationService.create(
                EnrolleeRelation
                        .builder()
                        .enrolleeId(proband.getId())
                        .targetEnrolleeId(member2.getId())
                        .relationshipType(RelationshipType.FAMILY)
                        .familyId(family.getId())
                        .familyRelationship("father")
                        .build(),
                getAuditInfo(info));

        List<EnrolleeRelation> relations = enrolleeRelationService.findRelationsForFamily(family.getId());

        assertEquals(3, relations.size());

        familyService.removeEnrollee(
                family.getShortcode(),
                member1.getShortcode(),
                studyEnvironment.getId(),
                getAuditInfo(info));

        members = familyEnrolleeService.findByFamilyId(family.getId());
        assertEquals(2, members.size());

        relations = enrolleeRelationService.findRelationsForFamily(family.getId());
        assertEquals(1, relations.size());

        assertFalse(relations.stream().anyMatch(r -> r.getEnrolleeId().equals(member1.getId())));
        assertFalse(relations.stream().anyMatch(r -> r.getTargetEnrolleeId().equals(member1.getId())));
    }

    @Test
    @Transactional
    public void testUpdateProband(TestInfo info) {
        StudyEnvironment studyEnvironment = studyEnvironmentFactory.buildPersisted(getTestName(info));
        Enrollee proband = enrolleeFactory.buildPersisted(getTestName(info), studyEnvironment);
        Family family = familyFactory.buildPersisted(getTestName(info), proband);

        Enrollee otherMember = enrolleeFactory.buildPersisted(getTestName(info), studyEnvironment);
        familyFactory.linkEnrolleeToFamily(otherMember, family);

        familyService.updateProband(
                family.getShortcode(),
                otherMember.getShortcode(),
                studyEnvironment.getId(),
                getAuditInfo(info));

        family = familyService.find(family.getId()).orElseThrow();
        assertEquals(otherMember.getId(), family.getProbandEnrolleeId());

    }

    @Test
    @Transactional
    public void testUpdateProbandMustBeInFamily(TestInfo info) {
        StudyEnvironment studyEnvironment = studyEnvironmentFactory.buildPersisted(getTestName(info));
        Enrollee proband = enrolleeFactory.buildPersisted(getTestName(info), studyEnvironment);
        Family family = familyFactory.buildPersisted(getTestName(info), proband);

        Enrollee randomOtherEnrollee = enrolleeFactory.buildPersisted(getTestName(info), studyEnvironment);

        assertThrows(IllegalArgumentException.class,
                () -> familyService.updateProband(
                        family.getShortcode(),
                        randomOtherEnrollee.getShortcode(),
                        studyEnvironment.getId(),
                        getAuditInfo(info)));
    }
}
