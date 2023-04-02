package bio.terra.pearl.api.participant.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import bio.terra.pearl.api.participant.BaseSpringBootTest;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.factory.participant.ParticipantUserFactory;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class AuthUtilServiceTests extends BaseSpringBootTest {
  @Autowired private EnrolleeFactory enrolleeFactory;
  @Autowired private ParticipantUserFactory participantUserFactory;
  @Autowired private AuthUtilService authUtilService;
  @Autowired private StudyEnvironmentService studyEnvironmentService;

  @Test
  @Transactional
  public void testAuthToEnrolleeAllowsIfParticipant() {
    Enrollee enrollee = enrolleeFactory.buildPersisted("testAuthToEnrolleeAllowsIfParticipant");
    Enrollee authEnrollee =
        authUtilService.authParticipantUserToEnrollee(
            enrollee.getParticipantUserId(), enrollee.getShortcode());
    assertThat(authEnrollee, notNullValue());
  }

  @Test
  @Transactional
  public void testAuthToEnrolleeDisallowsIfNotParticipant() {
    Enrollee enrollee =
        enrolleeFactory.buildPersisted("testAuthToEnrolleeDisallowsIfNotParticipant");
    StudyEnvironment studyEnv =
        studyEnvironmentService.find(enrollee.getStudyEnvironmentId()).get();
    ParticipantUser otherUser =
        participantUserFactory.buildPersisted(
            studyEnv.getEnvironmentName(), "testAuthToEnrolleeDisallowsIfNotParticipant");
    Assertions.assertThrows(
        PermissionDeniedException.class,
        () -> {
          authUtilService.authParticipantUserToEnrollee(otherUser.getId(), enrollee.getShortcode());
        });
  }
}
