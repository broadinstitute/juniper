package bio.terra.pearl.core.service.notification;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.notification.NotificationFactory;
import bio.terra.pearl.core.factory.notification.TriggerFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.model.notification.Notification;
import bio.terra.pearl.core.model.notification.Trigger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

class NotificationServiceTest extends BaseSpringBootTest {

    @Autowired NotificationService notificationService;
    @Autowired NotificationFactory notificationFactory;
    @Autowired TriggerFactory triggerFactory;
    @Autowired EnrolleeFactory enrolleeFactory;

    @Test
    @Transactional
    void findAllByConfigId(TestInfo info) {

        EnrolleeFactory.EnrolleeBundle bundle1 = enrolleeFactory.buildWithPortalUser(getTestName(info));
        EnrolleeFactory.EnrolleeBundle bundle2 = enrolleeFactory.buildWithPortalUser(getTestName(info));

        Trigger trigger1 = triggerFactory.buildPersisted(getTestName(info));
        Trigger trigger2 = triggerFactory.buildPersisted(getTestName(info));
        Trigger trigger3 = triggerFactory.buildPersisted(getTestName(info));

        Notification notif1 = notificationFactory.buildPersisted(bundle1, trigger1);
        Notification notif2 = notificationFactory.buildPersisted(bundle1, trigger1);
        Notification notif3 = notificationFactory.buildPersisted(bundle2, trigger1);
        Notification notif4 = notificationFactory.buildPersisted(bundle1, trigger2);

        List<Notification> trig1Notifs = notificationService.findAllByConfigId(trigger1.getId());
        List<Notification> trig2Notifs = notificationService.findAllByConfigId(trigger2.getId());
        List<Notification> trig3Notifs = notificationService.findAllByConfigId(trigger3.getId());

        Assertions.assertEquals(3, trig1Notifs.size());
        Assertions.assertEquals(1, trig2Notifs.size());
        Assertions.assertEquals(0, trig3Notifs.size());

        Assertions.assertTrue(trig1Notifs.contains(notif1));
        Assertions.assertTrue(trig1Notifs.contains(notif2));
        Assertions.assertTrue(trig1Notifs.contains(notif3));
        Assertions.assertTrue(trig2Notifs.contains(notif4));
    }
}