package bio.terra.pearl.core.dao.notification;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.DaoTestUtils;
import bio.terra.pearl.core.factory.notification.TriggerFactory;
import bio.terra.pearl.core.model.notification.Notification;
import bio.terra.pearl.core.model.notification.Trigger;
import bio.terra.pearl.core.model.notification.NotificationDeliveryStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class NotificationDaoTests extends BaseSpringBootTest {
  @Autowired
  private NotificationDao notificationDao;
  @Autowired
  private TriggerFactory triggerFactory;
  @Autowired
  private ObjectMapper objectMapper;

  @Test
  @Transactional
  public void testBasicCrud() {
    Trigger trigger = triggerFactory.buildPersisted("testBasicConfigCrud");
    Notification notification = Notification.builder()
        .deliveryType(trigger.getDeliveryType())
        .triggerId(trigger.getId())
        .deliveryStatus(NotificationDeliveryStatus.READY)
        .build();
    Notification savedNotification = notificationDao.create(notification);
    DaoTestUtils.assertGeneratedProperties(savedNotification);
  }

  @Test
  @Transactional
  public void testSavesCustomMessages() throws Exception {
    Trigger trigger = triggerFactory.buildPersisted("testSavesMessagesCrud");
    var messageMap = Map.of("foo", "bar", "baz", "boo");
    Notification notification = Notification.builder()
        .deliveryType(trigger.getDeliveryType())
        .triggerId(trigger.getId())
        .deliveryStatus(NotificationDeliveryStatus.READY)
        .customMessagesMap(messageMap)
        .build();
    Notification savedNotification = notificationDao.create(notification);
    DaoTestUtils.assertGeneratedProperties(savedNotification);
    Map<String, String> map = objectMapper.readValue(savedNotification.getCustomMessages(), Map.class);
    assertThat(map, equalTo(messageMap));
  }
}
