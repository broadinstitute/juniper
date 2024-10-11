package bio.terra.pearl.api.admin.service.notifications;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;

import bio.terra.pearl.api.admin.BaseSpringBootTest;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.notification.NotificationFactory;
import bio.terra.pearl.core.factory.notification.TriggerFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeBundle;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.factory.portal.PortalEnvironmentFactory;
import bio.terra.pearl.core.model.notification.*;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.notification.email.SendgridClient;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

public class SendgridEventServiceTests extends BaseSpringBootTest {

  @Autowired private SendgridEventService sendgridActivityService;
  @Autowired private NotificationFactory notificationFactory;
  @Autowired private EnrolleeFactory enrolleeFactory;
  @Autowired private PortalEnvironmentFactory portalEnvironmentFactory;
  @Autowired private StudyEnvironmentFactory studyEnvironmentFactory;
  @Autowired private TriggerFactory triggerFactory;

  @MockBean private SendgridClient sendgridClient;

  @Test
  public void testPagination() throws Exception {
    when(sendgridClient.getEvents(any(Instant.class), any(Instant.class), anyInt()))
        .thenReturn(mockEventPage(1000))
        .thenReturn(mockEventPage(527));

    List<SendgridEvent> events =
        sendgridActivityService.getAllRecentSendgridEvents(
            Instant.now().minus(30, ChronoUnit.DAYS), Instant.now());

    verify(sendgridClient, times(2)).getEvents(any(Instant.class), any(Instant.class), anyInt());
    assertThat(events, hasSize(1527));
  }

  @Test
  @Transactional
  public void testCorrelationSingleEvent(TestInfo info) {
    PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted(getTestName(info));
    StudyEnvironment studyEnv =
        studyEnvironmentFactory.buildPersisted(portalEnv, getTestName(info));
    EnrolleeBundle enrolleeBundle =
        enrolleeFactory.buildWithPortalUser(getTestName(info), portalEnv, studyEnv);

    Trigger trigger =
        triggerFactory.buildPersisted(
            Trigger.builder()
                .deliveryType(NotificationDeliveryType.EMAIL)
                .triggerType(TriggerType.TASK_REMINDER),
            studyEnv.getId(),
            portalEnv.getId());

    Notification notification =
        notificationFactory.buildPersisted(
            notificationFactory
                .builder(enrolleeBundle, trigger)
                .sendgridApiRequestId("fakeApiRequestId"));

    List<SendgridEvent> events = mockEventPage(1);
    List<SendgridEvent> correlatedEvents =
        sendgridActivityService.correlateNotificationsAndEvents(events);

    assertThat(
        correlatedEvents.stream().map(SendgridEvent::getNotificationId).toList(),
        equalTo(List.of(notification.getId())));
  }

  @Test
  @Transactional
  public void testCorrelationBatchedEvents(TestInfo info) {
    PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted(getTestName(info));
    StudyEnvironment studyEnv =
        studyEnvironmentFactory.buildPersisted(portalEnv, getTestName(info));
    EnrolleeBundle enrolleeBundle =
        enrolleeFactory.buildWithPortalUser(getTestName(info), portalEnv, studyEnv);

    Trigger trigger =
        triggerFactory.buildPersisted(
            Trigger.builder()
                .deliveryType(NotificationDeliveryType.EMAIL)
                .triggerType(TriggerType.TASK_REMINDER),
            studyEnv.getId(),
            portalEnv.getId());

    // Build two notifications that correlate to the same API request ID
    Notification notification1 =
        notificationFactory.buildPersisted(
            notificationFactory
                .builder(enrolleeBundle, trigger)
                .sendgridApiRequestId("fakeApiRequestId"));

    Notification notification2 =
        notificationFactory.buildPersisted(
            notificationFactory
                .builder(enrolleeBundle, trigger)
                .sendgridApiRequestId("fakeApiRequestId"));

    List<SendgridEvent> events = mockEventPage(1);
    List<SendgridEvent> correlatedEvents =
        sendgridActivityService.correlateNotificationsAndEvents(events);

    // We should not correlate the event if there are multiple
    // notifications for the same API request ID
    assertThat(correlatedEvents.stream().findFirst().get().getNotificationId(), equalTo(null));
  }

  private List<SendgridEvent> mockEventPage(int numEvents) {
    List<SendgridEvent> events = new ArrayList<>();
    for (int i = 0; i < numEvents; i++) {
      events.add(mockSendgridEvent());
    }
    return events;
  }

  private SendgridEvent mockSendgridEvent() {
    return SendgridEvent.builder()
        .msgId("msgId")
        .subject("subject")
        .toEmail("toEmail")
        .fromEmail("fromEmail")
        .apiRequestId("fakeApiRequestId")
        .status("status")
        .opensCount(1)
        .clicksCount(1)
        .lastEventTime(Instant.now())
        .build();
  }
}
