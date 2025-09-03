package bio.terra.pearl.core.service.notification;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.dao.dataimport.TimeShiftDao;
import bio.terra.pearl.core.dao.notification.NotificationDao;
import bio.terra.pearl.core.dao.workflow.ParticipantTaskDao;
import bio.terra.pearl.core.factory.StudyEnvironmentBundle;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.kit.KitRequestFactory;
import bio.terra.pearl.core.factory.kit.KitTypeFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeBundle;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.factory.participant.ParticipantTaskFactory;
import bio.terra.pearl.core.factory.portal.PortalEnvironmentFactory;
import bio.terra.pearl.core.factory.survey.SurveyFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.kit.KitType;
import bio.terra.pearl.core.model.notification.Notification;
import bio.terra.pearl.core.model.notification.NotificationDeliveryType;
import bio.terra.pearl.core.model.notification.Trigger;
import bio.terra.pearl.core.model.notification.TriggerType;
import bio.terra.pearl.core.model.participant.EnrolleeSourceType;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import bio.terra.pearl.core.model.workflow.TaskType;
import bio.terra.pearl.core.service.kit.pepper.PepperKitStatus;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

/**
 * See ParticipantTaskDaoTests for detailed tests of the reminder timing logic.  This is a high-level test
 * to confirm everything works together.
 */
public class EnrolleeReminderServiceTests extends BaseSpringBootTest {
  @Test
  @Transactional
  public void testConsentReminders(TestInfo info) {
    PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted(getTestName(info));
    StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(portalEnv, getTestName(info));
      EnrolleeBundle enrolleeBundle = enrolleeFactory.buildWithPortalUser(getTestName(info), portalEnv, studyEnv);
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

  @Test
  @Transactional
  public void testSurveyRemindersDontSendToUnconsented(TestInfo info) {
    PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted(getTestName(info));
    StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(portalEnv, getTestName(info));
    EnrolleeBundle enrolleeBundle = enrolleeFactory.buildWithPortalUser(getTestName(info), portalEnv, studyEnv);
    ParticipantTask newTask1 = participantTaskFactory.buildPersisted(enrolleeBundle, TaskStatus.NEW, TaskType.SURVEY);

    Trigger config = Trigger.builder()
            .triggerType(TriggerType.TASK_REMINDER)
            .taskType(TaskType.SURVEY)
            .afterMinutesIncomplete(0)
            .deliveryType(NotificationDeliveryType.EMAIL)
            .studyEnvironmentId(studyEnv.getId())
            .portalEnvironmentId(portalEnv.getId())
            .build();
    triggerService.create(config);
    enrolleeReminderService.sendTaskReminders(studyEnv);

    List<Notification> notificationList = notificationDao.findByEnrolleeId(enrolleeBundle.enrollee().getId());
    assertThat(notificationList, hasSize(0));
  }

  @Test
  @Transactional
  public void testFiltersByKitType(TestInfo info) {
    String testName = getTestName(info);
    KitType bloodKitType = kitTypeFactory.buildPersisted("Blood");
    KitType salivaKitType = kitTypeFactory.buildPersisted("Saliva");
    PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted(testName);
    StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(portalEnv, testName);
    EnrolleeBundle enrolleeBundle1 = enrolleeFactory.buildWithPortalUser(testName, portalEnv, studyEnv, true);
    EnrolleeBundle enrolleeBundle2 = enrolleeFactory.buildWithPortalUser(testName, portalEnv, studyEnv, true);

    KitRequest kitRequest1 = kitRequestFactory.buildPersisted(testName, enrolleeBundle1.enrollee(), PepperKitStatus.CREATED, bloodKitType.getId());
    participantTaskFactory.buildPersisted(enrolleeBundle1, TaskStatus.NEW, kitRequest1.getId(), bloodKitType);

    KitRequest kitRequest2 = kitRequestFactory.buildPersisted(testName, enrolleeBundle2.enrollee(), PepperKitStatus.CREATED, salivaKitType.getId());
    participantTaskFactory.buildPersisted(enrolleeBundle2, TaskStatus.NEW, kitRequest2.getId(), salivaKitType);

    Trigger config = Trigger.builder()
            .triggerType(TriggerType.TASK_REMINDER)
            .taskType(TaskType.KIT_REQUEST)
            .filterTargetStableIds(List.of(bloodKitType.getName()))
            .afterMinutesIncomplete(0)
            .deliveryType(NotificationDeliveryType.EMAIL)
            .studyEnvironmentId(studyEnv.getId())
            .portalEnvironmentId(portalEnv.getId())
            .build();
    triggerService.create(config);
    enrolleeReminderService.sendTaskReminders(studyEnv);

    List<Notification> notificationList = notificationDao.findByEnrolleeId(enrolleeBundle1.enrollee().getId());
    assertThat(notificationList, hasSize(1));
    notificationList = notificationDao.findByEnrolleeId(enrolleeBundle2.enrollee().getId());
    assertThat(notificationList, hasSize(0));
  }

  @Test
  @Transactional
  public void testSendsKitReminders(TestInfo info) {
    String testName = getTestName(info);
    KitType bloodKitType = kitTypeFactory.buildPersisted("Blood");
    PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted(testName);
    StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(portalEnv, testName);

    //  create two enrollees, one with a pending kit request task, and one with a completed kit request task
    EnrolleeBundle enrolleeBundle1 = enrolleeFactory.buildWithPortalUser(testName, portalEnv, studyEnv, true);
    EnrolleeBundle enrolleeBundle2 = enrolleeFactory.buildWithPortalUser(testName, portalEnv, studyEnv, true);

    KitRequest kitRequest1 = kitRequestFactory.buildPersisted(testName, enrolleeBundle1.enrollee(), PepperKitStatus.CREATED, bloodKitType.getId());
    participantTaskFactory.buildPersisted(enrolleeBundle1, TaskStatus.NEW, kitRequest1.getId(), bloodKitType);

    KitRequest kitRequest2 = kitRequestFactory.buildPersisted(testName, enrolleeBundle2.enrollee(), PepperKitStatus.CREATED, bloodKitType.getId());
    participantTaskFactory.buildPersisted(enrolleeBundle2, TaskStatus.COMPLETE, kitRequest2.getId(), bloodKitType);

    Trigger config = Trigger.builder()
            .triggerType(TriggerType.TASK_REMINDER)
            .taskType(TaskType.KIT_REQUEST)
            .afterMinutesIncomplete(0)
            .deliveryType(NotificationDeliveryType.EMAIL)
            .studyEnvironmentId(studyEnv.getId())
            .portalEnvironmentId(portalEnv.getId())
            .build();
    triggerService.create(config);
    enrolleeReminderService.sendTaskReminders(studyEnv);

    // only the pending task should receive a reminder
    List<Notification> notificationList = notificationDao.findByEnrolleeId(enrolleeBundle1.enrollee().getId());
    assertThat(notificationList, hasSize(1));
    notificationList = notificationDao.findByEnrolleeId(enrolleeBundle2.enrollee().getId());
    assertThat(notificationList, hasSize(0));
  }


  @Test
  @Transactional
  public void testRemindersDontSendToImported(TestInfo info) {
    PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted(getTestName(info));
    StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(portalEnv, getTestName(info));
    EnrolleeBundle enrolleeBundle = enrolleeFactory.buildWithPortalUser(getTestName(info), portalEnv, studyEnv, true);
    enrolleeBundle.enrollee().setSource(EnrolleeSourceType.IMPORT);
    enrolleeService.update(enrolleeBundle.enrollee());
    ParticipantTask newTask1 = participantTaskFactory.buildPersisted(enrolleeBundle, TaskStatus.NEW, TaskType.CONSENT);
    Trigger config = Trigger.builder()
            .triggerType(TriggerType.TASK_REMINDER)
            .taskType(TaskType.CONSENT)
            .afterMinutesIncomplete(0)
            .deliveryType(NotificationDeliveryType.EMAIL)
            .studyEnvironmentId(studyEnv.getId())
            .portalEnvironmentId(portalEnv.getId())
            .build();
    triggerService.create(config);
    enrolleeReminderService.sendTaskReminders(studyEnv);

    List<Notification> notificationList = notificationDao.findByEnrolleeId(enrolleeBundle.enrollee().getId());
    assertThat(notificationList, hasSize(0));
  }

  @Test
  @Transactional
  public void testSendFilteredReminderEvenIfOtherReminderSent(TestInfo info) {
    // create 2 surveys
    StudyEnvironmentBundle studyEnvBundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);
    Survey survey1 = surveyFactory.buildPersisted(getTestName(info), studyEnvBundle.getPortal().getId());
    Survey survey2 = surveyFactory.buildPersisted(getTestName(info), studyEnvBundle.getPortal().getId());

    surveyFactory.attachToEnv(survey1, studyEnvBundle.getStudyEnv().getId(), true);
    surveyFactory.attachToEnv(survey2, studyEnvBundle.getStudyEnv().getId(), true);
    // create reminder config for all surveys

    Trigger configAllSurveys = Trigger.builder()
            .triggerType(TriggerType.TASK_REMINDER)
            .taskType(TaskType.SURVEY)
            .afterMinutesIncomplete(0)
            .deliveryType(NotificationDeliveryType.EMAIL)
            .studyEnvironmentId(studyEnvBundle.getStudyEnv().getId())
            .portalEnvironmentId(studyEnvBundle.getPortalEnv().getId())
            .build();

    triggerService.create(configAllSurveys);

    Trigger configOnlySurvey1 = Trigger.builder()
            .triggerType(TriggerType.TASK_REMINDER)
            .taskType(TaskType.SURVEY)
            .filterTargetStableIds(List.of(survey1.getStableId()))
            .afterMinutesIncomplete(0)
            .deliveryType(NotificationDeliveryType.EMAIL)
            .studyEnvironmentId(studyEnvBundle.getStudyEnv().getId())
            .portalEnvironmentId(studyEnvBundle.getPortalEnv().getId())
            .build();

    triggerService.create(configOnlySurvey1);

    // create enrollee with both surveys

    EnrolleeBundle enrolleeBundle = enrolleeFactory.buildWithPortalUser(getTestName(info), studyEnvBundle.getPortalEnv(), studyEnvBundle.getStudyEnv(), true);

    participantTaskFactory.buildPersisted(enrolleeBundle, survey1.getStableId(), TaskStatus.NEW, TaskType.SURVEY);
    participantTaskFactory.buildPersisted(enrolleeBundle, survey2.getStableId(), TaskStatus.NEW, TaskType.SURVEY);

    // ensure both get sent
    enrolleeReminderService.sendTaskReminders(studyEnvBundle.getStudyEnv());

    List<Notification> notificationList = notificationDao.findByEnrolleeId(enrolleeBundle.enrollee().getId());
    assertThat(notificationList, hasSize(2));
  }

  @Test
  @Transactional
  public void testSendMultipleFilteredReminders(TestInfo info) {
    // create 2 surveys
    StudyEnvironmentBundle studyEnvBundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);
    Survey survey1 = surveyFactory.buildPersisted(getTestName(info), studyEnvBundle.getPortal().getId());
    Survey survey2 = surveyFactory.buildPersisted(getTestName(info), studyEnvBundle.getPortal().getId());

    surveyFactory.attachToEnv(survey1, studyEnvBundle.getStudyEnv().getId(), true);
    surveyFactory.attachToEnv(survey2, studyEnvBundle.getStudyEnv().getId(), true);
    // create reminder config for all surveys

    Trigger configOnlySurvey1 = Trigger.builder()
            .triggerType(TriggerType.TASK_REMINDER)
            .taskType(TaskType.SURVEY)
            .afterMinutesIncomplete(0)
            .filterTargetStableIds(List.of(survey1.getStableId()))
            .deliveryType(NotificationDeliveryType.EMAIL)
            .studyEnvironmentId(studyEnvBundle.getStudyEnv().getId())
            .portalEnvironmentId(studyEnvBundle.getPortalEnv().getId())
            .build();

    triggerService.create(configOnlySurvey1);

    Trigger configOnlySurvey2 = Trigger.builder()
            .triggerType(TriggerType.TASK_REMINDER)
            .taskType(TaskType.SURVEY)
            .filterTargetStableIds(List.of(survey2.getStableId()))
            .afterMinutesIncomplete(0)
            .deliveryType(NotificationDeliveryType.EMAIL)
            .studyEnvironmentId(studyEnvBundle.getStudyEnv().getId())
            .portalEnvironmentId(studyEnvBundle.getPortalEnv().getId())
            .build();

    triggerService.create(configOnlySurvey2);

    // create enrollee with both surveys

    EnrolleeBundle enrolleeBundle = enrolleeFactory.buildWithPortalUser(getTestName(info), studyEnvBundle.getPortalEnv(), studyEnvBundle.getStudyEnv(), true);

    participantTaskFactory.buildPersisted(enrolleeBundle, survey1.getStableId(), TaskStatus.NEW, TaskType.SURVEY);
    participantTaskFactory.buildPersisted(enrolleeBundle, survey2.getStableId(), TaskStatus.NEW, TaskType.SURVEY);

    // ensure both get sent
    enrolleeReminderService.sendTaskReminders(studyEnvBundle.getStudyEnv());

    List<Notification> notificationList = notificationDao.findByEnrolleeId(enrolleeBundle.enrollee().getId());
    assertThat(notificationList, hasSize(2));
  }

  @Test
  @Transactional
  public void testSendLongitudinalReminders(TestInfo info) {

    String testName = getTestName(info);
    StudyEnvironmentBundle studyEnvBundle = studyEnvironmentFactory.buildBundle(testName, EnvironmentName.sandbox);
    PortalEnvironment portalEnv = studyEnvBundle.getPortalEnv();
    StudyEnvironment studyEnv = studyEnvBundle.getStudyEnv();

    EnrolleeBundle enrolleeBundle = enrolleeFactory.buildWithPortalUser(testName, portalEnv, studyEnv, true);
    Survey survey1 = surveyFactory.buildPersisted(testName, studyEnvBundle.getPortal().getId());
    surveyFactory.attachToEnv(survey1, studyEnv.getId(), true);

    ParticipantTask firstTask = participantTaskFactory.buildPersisted(enrolleeBundle, survey1.getStableId(), TaskStatus.NEW, TaskType.SURVEY);

    Trigger config = Trigger.builder()
            .triggerType(TriggerType.TASK_REMINDER)
            .taskType(TaskType.SURVEY)
            .afterMinutesIncomplete(0)
            .deliveryType(NotificationDeliveryType.EMAIL)
            .studyEnvironmentId(studyEnv.getId())
            .portalEnvironmentId(portalEnv.getId())
            .maxNumNotifications(1)
            .build();
    triggerService.create(config);
    enrolleeReminderService.sendTaskReminders(studyEnv);

    List<Notification> notificationList = notificationDao.findByEnrolleeId(enrolleeBundle.enrollee().getId());
    assertThat(notificationList, hasSize(1));

    Notification firstNotification = notificationList.get(0);

    timeShiftDao.changeNotificationCreationTime(firstNotification.getId(), Instant.now().minus(365, ChronoUnit.DAYS));

    firstTask = participantTaskDao.find(firstTask.getId()).orElseThrow();
    firstTask.setStatus(TaskStatus.IN_PROGRESS);
    participantTaskDao.update(firstTask);
    timeShiftDao.changeTasksCreationTime(List.of(firstTask.getId()), Instant.now().minus(365, ChronoUnit.DAYS));

    // shouldn't send when it's old
    enrolleeReminderService.sendTaskReminders(studyEnv);

    notificationList = notificationDao.findByEnrolleeId(enrolleeBundle.enrollee().getId());
    assertThat(notificationList, hasSize(1));

    // create new task
    ParticipantTask newTask = participantTaskFactory.buildPersisted(enrolleeBundle, survey1.getStableId(), TaskStatus.NEW, TaskType.SURVEY);

    // should send again now since there's a new task
    enrolleeReminderService.sendTaskReminders(studyEnv);

    notificationList = notificationDao.findByEnrolleeId(enrolleeBundle.enrollee().getId());
    assertThat(notificationList, hasSize(2));
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
  @Autowired
  private EnrolleeService enrolleeService;
  @Autowired
  private KitTypeFactory kitTypeFactory;
  @Autowired
  private KitRequestFactory kitRequestFactory;
  @Autowired
  private SurveyFactory surveyFactory;
  @Autowired
  private ParticipantTaskDao participantTaskDao;
  @Autowired
  private TimeShiftDao timeShiftDao;
}
