package bio.terra.pearl.api.participant.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import bio.terra.pearl.api.participant.BaseSpringBootTest;
import bio.terra.pearl.core.factory.participant.EnrolleeBundle;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.factory.participant.ParticipantUserFactory;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.EnrolleeRelation;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.participant.RelationshipType;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import bio.terra.pearl.core.service.participant.EnrolleeRelationService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class AuthUtilServiceTests extends BaseSpringBootTest {
  @Autowired private EnrolleeFactory enrolleeFactory;
  @Autowired private ParticipantUserFactory participantUserFactory;
  @Autowired private AuthUtilService authUtilService;
  @Autowired private StudyEnvironmentService studyEnvironmentService;
  @Autowired private EnrolleeRelationService enrolleeRelationService;

  @Test
  @Transactional
  public void testAuthToEnrolleeAllowsIfParticipant(TestInfo info) {
    Enrollee enrollee = enrolleeFactory.buildPersisted(getTestName(info));
    Enrollee authEnrollee =
        authUtilService.authParticipantUserToEnrollee(
            enrollee.getParticipantUserId(), enrollee.getShortcode());
    assertThat(authEnrollee, notNullValue());
  }

  @Test
  @Transactional
  public void testAuthToEnrolleeDisallowsIfNotParticipant(TestInfo info) {
    Enrollee enrollee = enrolleeFactory.buildPersisted(getTestName(info));
    StudyEnvironment studyEnv =
        studyEnvironmentService.find(enrollee.getStudyEnvironmentId()).get();
    ParticipantUser otherUser =
        participantUserFactory.buildPersisted(studyEnv.getEnvironmentName(), getTestName(info));
    Assertions.assertThrows(
        PermissionDeniedException.class,
        () -> {
          authUtilService.authParticipantUserToEnrollee(otherUser.getId(), enrollee.getShortcode());
        });
  }

  @Test
  @Transactional
  public void testAuthToPortalParticipantUserAllowsIfParticipant(TestInfo info) {
    EnrolleeBundle bundle = enrolleeFactory.buildWithPortalUser(getTestName(info));
    PortalParticipantUser ppUser =
        authUtilService.authParticipantUserToPortalParticipantUser(
            bundle.enrollee().getParticipantUserId(), bundle.portalParticipantUser().getId());
    Assertions.assertEquals(bundle.portalParticipantUser(), ppUser);
  }

  @Test
  @Transactional
  public void testAuthToPortalParticipantUserAllowsIfProxy(TestInfo info) {

    EnrolleeBundle proxyBundle = enrolleeFactory.buildWithPortalUser(getTestName(info));
    EnrolleeBundle targetBundle = enrolleeFactory.buildWithPortalUser(getTestName(info));

    enrolleeRelationService.create(
        EnrolleeRelation.builder()
            .enrolleeId(proxyBundle.enrollee().getId())
            .targetEnrolleeId(targetBundle.enrollee().getId())
            .relationshipType(RelationshipType.PROXY)
            .build(),
        getAuditInfo(info));

    PortalParticipantUser ppUser =
        authUtilService.authParticipantUserToPortalParticipantUser(
            proxyBundle.enrollee().getParticipantUserId(),
            targetBundle.portalParticipantUser().getId());

    Assertions.assertEquals(targetBundle.portalParticipantUser(), ppUser);
  }

  @Test
  @Transactional
  public void testAuthToPortalParticipantUserDisallowsIfNotParticipantOrProxy(TestInfo info) {
    EnrolleeBundle bundle = enrolleeFactory.buildWithPortalUser(getTestName(info));
    StudyEnvironment studyEnv =
        studyEnvironmentService.find(bundle.enrollee().getStudyEnvironmentId()).get();
    ParticipantUser otherUser =
        participantUserFactory.buildPersisted(studyEnv.getEnvironmentName(), getTestName(info));
    Assertions.assertThrows(
        NotFoundException.class,
        () ->
            authUtilService.authParticipantUserToPortalParticipantUser(
                bundle.enrollee().getParticipantUserId(), otherUser.getId()));
  }
}
