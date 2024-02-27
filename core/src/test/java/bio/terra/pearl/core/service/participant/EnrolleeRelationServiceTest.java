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
                List.of(targetEnrollee1, targetEnrollee2)));

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
                List.of(targetEnrollee1, targetEnrollee2)));

    }
}