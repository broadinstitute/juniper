package bio.terra.pearl.core.service.notification;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.dao.notification.SendgridEventDao;
import bio.terra.pearl.core.factory.notification.NotificationFactory;
import bio.terra.pearl.core.factory.notification.TriggerFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeBundle;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.model.notification.Notification;
import bio.terra.pearl.core.model.notification.SendgridEvent;
import bio.terra.pearl.core.model.notification.Trigger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

class NotificationServiceTest extends BaseSpringBootTest {

    @Autowired
    NotificationService notificationService;
    @Autowired
    NotificationFactory notificationFactory;
    @Autowired
    TriggerFactory triggerFactory;
    @Autowired
    EnrolleeFactory enrolleeFactory;
    @Autowired
    SendgridEventDao sendgridEventDao;

    @Test
    @Transactional
    void findAllByConfigId(TestInfo info) {

        EnrolleeBundle bundle1 = enrolleeFactory.buildWithPortalUser(getTestName(info));
        EnrolleeBundle bundle2 = enrolleeFactory.buildWithPortalUser(getTestName(info));

        Trigger trigger1 = triggerFactory.buildPersisted(getTestName(info));
        Trigger trigger2 = triggerFactory.buildPersisted(getTestName(info));
        Trigger trigger3 = triggerFactory.buildPersisted(getTestName(info));

        Notification notif1 = notificationFactory.buildPersisted(bundle1, trigger1);
        Notification notif2 = notificationFactory.buildPersisted(bundle1, trigger1);
        Notification notif3 = notificationFactory.buildPersisted(bundle2, trigger1);
        Notification notif4 = notificationFactory.buildPersisted(bundle1, trigger2);

        List<Notification> trig1Notifs = notificationService.findAllByConfigId(trigger1.getId(), false);
        List<Notification> trig2Notifs = notificationService.findAllByConfigId(trigger2.getId(), false);
        List<Notification> trig3Notifs = notificationService.findAllByConfigId(trigger3.getId(), false);

        Assertions.assertEquals(3, trig1Notifs.size());
        Assertions.assertEquals(1, trig2Notifs.size());
        Assertions.assertEquals(0, trig3Notifs.size());

        Assertions.assertTrue(trig1Notifs.contains(notif1));
        Assertions.assertTrue(trig1Notifs.contains(notif2));
        Assertions.assertTrue(trig1Notifs.contains(notif3));
        Assertions.assertTrue(trig2Notifs.contains(notif4));
    }

    @Test
    @Transactional
    void findAllByConfigIdWithEnrollees(TestInfo info) {

        EnrolleeBundle bundle1 = enrolleeFactory.buildWithPortalUser(getTestName(info));
        EnrolleeBundle bundle2 = enrolleeFactory.buildWithPortalUser(getTestName(info));

        Trigger trigger1 = triggerFactory.buildPersisted(getTestName(info));

        Notification notif1 = notificationFactory.buildPersisted(bundle1, trigger1);
        Notification notif2 = notificationFactory.buildPersisted(bundle2, trigger1);

        List<Notification> notifs = notificationService.findAllByConfigId(trigger1.getId(), true);

        Assertions.assertEquals(2, notifs.size());

        Notification foundNotif1 = notifs.stream().filter(notif -> notif.getId().equals(notif1.getId())).findFirst().orElseThrow();
        Notification foundNotif2 = notifs.stream().filter(notif -> notif.getId().equals(notif2.getId())).findFirst().orElseThrow();

        Assertions.assertEquals(bundle1.enrollee(), foundNotif1.getEnrollee());
        Assertions.assertEquals(bundle2.enrollee(), foundNotif2.getEnrollee());
    }

    @Test
    @Transactional
    void findAllByConfigIdAttachSendgridEvents(TestInfo info) {

        EnrolleeBundle bundle1 = enrolleeFactory.buildWithPortalUser(getTestName(info));
        EnrolleeBundle bundle2 = enrolleeFactory.buildWithPortalUser(getTestName(info));
        EnrolleeBundle bundle3 = enrolleeFactory.buildWithPortalUser(getTestName(info));

        Trigger trigger1 = triggerFactory.buildPersisted(getTestName(info));

        Notification notif1 = notificationFactory.buildPersisted(bundle1, trigger1);
        Notification notif2 = notificationFactory.buildPersisted(bundle2, trigger1);
        Notification notif3 = notificationFactory.buildPersisted(bundle2, trigger1);
        Notification notif4 = notificationFactory.buildPersisted(bundle3, trigger1);

        sendgridEventDao.create(SendgridEvent.builder()
                .notificationId(notif1.getId())
                .status("delivered")
                .msgId("mock-id-1")
                .build());

        sendgridEventDao.create(SendgridEvent.builder()
                .notificationId(notif4.getId())
                .status("opened")
                .msgId("mock-id-2")
                .build());

        sendgridEventDao.create(SendgridEvent.builder()
                .notificationId(notif3.getId())
                .status("n/a")
                .msgId("mock-id-3")
                .build());

        List<Notification> notifs = notificationService.findAllByConfigId(trigger1.getId(), true);

        Assertions.assertEquals(4, notifs.size());

        Notification foundNotif1 = notifs.stream().filter(notif -> notif.getId().equals(notif1.getId())).findFirst().orElseThrow();
        Notification foundNotif2 = notifs.stream().filter(notif -> notif.getId().equals(notif2.getId())).findFirst().orElseThrow();
        Notification foundNotif3 = notifs.stream().filter(notif -> notif.getId().equals(notif3.getId())).findFirst().orElseThrow();
        Notification foundNotif4 = notifs.stream().filter(notif -> notif.getId().equals(notif4.getId())).findFirst().orElseThrow();


        Assertions.assertEquals(foundNotif1.getEventDetails().getStatus(), "delivered");
        Assertions.assertNull(foundNotif2.getEventDetails());
        Assertions.assertEquals(foundNotif3.getEventDetails().getStatus(), "n/a");
        Assertions.assertEquals(foundNotif4.getEventDetails().getStatus(), "opened");
    }
}
