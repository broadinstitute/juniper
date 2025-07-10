package bio.terra.pearl.api.participant.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import bio.terra.pearl.api.participant.BaseSpringBootTest;
import bio.terra.pearl.core.factory.StudyEnvironmentBundle;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeBundle;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.workflow.Event;
import bio.terra.pearl.core.model.workflow.EventClass;
import bio.terra.pearl.core.service.workflow.EventService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

class ExternalEventExtServiceTest extends BaseSpringBootTest {

  @Autowired EnrolleeFactory enrolleeFactory;
  @Autowired StudyEnvironmentFactory studyEnvironmentFactory;
  @Autowired EventService eventService;
  @Autowired ExternalEventExtService externalEventExtService;

  @Test
  @Transactional
  void testPublishesFailedLoginEvent(TestInfo info) {

    StudyEnvironmentBundle studyEnvBundle =
        studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);

    EnrolleeBundle enrolleeBundle =
        enrolleeFactory.buildWithPortalUser(
            getTestName(info), studyEnvBundle.getPortalEnv(), studyEnvBundle.getStudyEnv());

    ParticipantUser participantUser = enrolleeBundle.participantUser();

    externalEventExtService.trackFailedLogin(
        studyEnvBundle.getPortal().getShortcode(),
        EnvironmentName.irb, // different environment to test
        participantUser.getUsername());

    // verify no event, track happened in irb
    List<Event> failedLoginEvents =
        eventService.findAllByStudyEnvAndClass(
            studyEnvBundle.getStudyEnv().getId(), EventClass.ENROLLEE_FAILED_LOGIN_EVENT);

    assertEquals(0, failedLoginEvents.size());

    externalEventExtService.trackFailedLogin(
        studyEnvBundle.getPortal().getShortcode(),
        studyEnvBundle.getStudyEnv().getEnvironmentName(),
        participantUser.getUsername());

    // verify event was tracked
    failedLoginEvents =
        eventService.findAllByStudyEnvAndClass(
            studyEnvBundle.getStudyEnv().getId(), EventClass.ENROLLEE_FAILED_LOGIN_EVENT);

    assertEquals(1, failedLoginEvents.size());

    Event failedLoginEvent = failedLoginEvents.getFirst();

    assertEquals(EventClass.ENROLLEE_FAILED_LOGIN_EVENT, failedLoginEvent.getEventClass());
    assertEquals(enrolleeBundle.enrollee().getId(), failedLoginEvent.getEnrolleeId());
  }
}
