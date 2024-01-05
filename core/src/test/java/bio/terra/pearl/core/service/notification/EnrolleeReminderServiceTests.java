package bio.terra.pearl.core.service.notification;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.dao.notification.NotificationDao;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.factory.participant.ParticipantTaskFactory;
import bio.terra.pearl.core.factory.portal.PortalEnvironmentFactory;
import bio.terra.pearl.core.model.notification.Notification;
import bio.terra.pearl.core.model.notification.Trigger;
import bio.terra.pearl.core.model.notification.NotificationDeliveryType;
import bio.terra.pearl.core.model.notification.TriggerType;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import bio.terra.pearl.core.model.workflow.TaskType;
import java.util.List;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * See ParticipantTaskDaoTests for detailed tests of the reminder timing logic.  This is a high-level test
 * to confirm everything works together.
 */
public class EnrolleeReminderServiceTests extends BaseSpringBootTest {
  @Test
  @Transactional
  public void testConsentReminders() {
    PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted("testConsentReminders");
    StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(portalEnv, "testConsentReminders");
    var enrolleeBundle = enrolleeFactory.buildWithPortalUser("testConsentReminders", portalEnv, studyEnv);
    ParticipantTask newTask1 = participantTaskFactory.buildPersisted(enrolleeBundle, TaskStatus.NEW, TaskType.CONSENT);

    Trigger config = Trigger.builder()
        .triggerType(TriggerType.TASK_REMINDER)
        .taskType(TaskType.CONSENT)
        .afterMinutesIncomplete(0)
        .deliveryType(NotificationDeliveryType.EMAIL)
        .studyEnvironmentId(studyEnv.getId())
        .portalEnvironmentId(portalEnv.getId())
        .build();
    Trigger savedConfig = triggerService.create(config);
    enrolleeReminderService.sendTaskReminders(studyEnv);

    List<Notification> notificationList = notificationDao.findByEnrolleeId(enrolleeBundle.enrollee().getId());
    assertThat(notificationList, hasSize(1));
    assertThat(notificationList.get(0).getTriggerId(), equalTo(savedConfig.getId()));
  }
  @Autowired
  private ParticipantTaskFactory participantTaskFactory;
  @Autowired
  private EnrolleeReminderService enrolleeReminderService;
  @Autowired
  private StudyEnvironmentFactory studyEnvironmentFactory;
  @Autowired
  private PortalEnvironmentFactory portalEnvironmentFactory;
  @Autowired
  private NotificationDao notificationDao;
  @Autowired
  private TriggerService triggerService;
  @Autowired
  private EnrolleeFactory enrolleeFactory;
}
