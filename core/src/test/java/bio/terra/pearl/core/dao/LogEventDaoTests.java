package bio.terra.pearl.core.dao;


import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.dao.log.LogEventDao;
import bio.terra.pearl.core.model.log.LogEvent;
import bio.terra.pearl.core.model.log.LogEventSource;
import bio.terra.pearl.core.model.log.LogEventType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class LogEventDaoTests extends BaseSpringBootTest {
  @Test
  @Transactional
  public void testCreate() {
    LogEvent event = LogEvent.builder()
        .eventType(LogEventType.ERROR)
        .eventName("TEST")
        .eventSource(LogEventSource.EXTERNAL)
        .eventDetail("{stuff: true}")
        .build();
    LogEvent createdEvent = logEventDao.create(event);
    assertThat(createdEvent.getId(), notNullValue());
  }

  @Autowired
  private LogEventDao logEventDao;
}
