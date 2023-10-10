package bio.terra.pearl.api.admin.service.notifications;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;

import bio.terra.pearl.api.admin.BaseSpringBootTest;
import bio.terra.pearl.core.model.notification.SendgridEvent;
import bio.terra.pearl.core.service.notification.email.SendgridClient;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

public class SendgridEventServiceTests extends BaseSpringBootTest {

  @Autowired private SendgridEventService sendgridActivityService;

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
        .status("status")
        .opensCount(1)
        .clicksCount(1)
        .lastEventTime(Instant.now())
        .build();
  }
}
