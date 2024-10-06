package bio.terra.pearl.core.dao.participant;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.dao.workflow.ParticipantTaskDao;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.notification.NotificationFactory;
import bio.terra.pearl.core.factory.notification.TriggerFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeBundle;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.factory.participant.ParticipantTaskFactory;
import bio.terra.pearl.core.factory.portal.PortalEnvironmentFactory;
import bio.terra.pearl.core.model.notification.NotificationDeliveryStatus;
import bio.terra.pearl.core.model.notification.NotificationDeliveryType;
import bio.terra.pearl.core.model.notification.Trigger;
import bio.terra.pearl.core.model.notification.TriggerType;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import bio.terra.pearl.core.model.workflow.TaskType;
import bio.terra.pearl.core.service.workflow.ParticipantTaskService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

public class ParticipantTaskDaoTests extends BaseSpringBootTest {
    @Test
    @Transactional
    public void testFindByStatusAndTimeOneTask(TestInfo info) {
        PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted(getTestName(info));
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(portalEnv, getTestName(info));
        EnrolleeBundle enrolleeBundle = enrolleeFactory.buildWithPortalUser(getTestName(info), portalEnv, studyEnv);
        ParticipantTask newTask1 = participantTaskFactory.buildPersisted(enrolleeBundle, TaskStatus.NEW, TaskType.CONSENT);

        // check status filtering
        List<ParticipantTaskDao.EnrolleeWithTasks> tasks = participantTaskDao.findByStatusAndTime(studyEnv.getId(), newTask1.getTaskType(),
                Duration.ofSeconds(0), Duration.ofHours(1), Duration.ofSeconds(1), List.of(TaskStatus.NEW));
        assertThat(tasks, hasSize(1));

        List<ParticipantTaskDao.EnrolleeWithTasks> inProgressTasks = participantTaskDao.findByStatusAndTime(studyEnv.getId(), newTask1.getTaskType(),
                Duration.ofSeconds(0), Duration.ofHours(1), Duration.ofSeconds(1), List.of(TaskStatus.IN_PROGRESS));
        assertThat(inProgressTasks, hasSize(0));

        List<ParticipantTaskDao.EnrolleeWithTasks> inProgressAndNewTasks = participantTaskDao.findByStatusAndTime(studyEnv.getId(), newTask1.getTaskType(),
                Duration.ofSeconds(0), Duration.ofHours(1), Duration.ofSeconds(1), List.of(TaskStatus.NEW, TaskStatus.IN_PROGRESS));
        assertThat(inProgressAndNewTasks, hasSize(1));

        List<ParticipantTaskDao.EnrolleeWithTasks> minsAgoTasks = participantTaskDao.findByStatusAndTime(studyEnv.getId(), newTask1.getTaskType(),
                Duration.ofMinutes(5), Duration.ofHours(1), Duration.ofSeconds(1), List.of(TaskStatus.NEW));
        assertThat(minsAgoTasks, hasSize(0));


        // Now check that it filters out tasks if there is a recent notification
        Trigger trigger = triggerFactory.buildPersisted(Trigger.builder()
                .deliveryType(NotificationDeliveryType.EMAIL)
                .triggerType(TriggerType.TASK_REMINDER),
                                studyEnv.getId(), portalEnv.getId());
        notificationFactory.buildPersisted(
                notificationFactory.builder(enrolleeBundle, trigger).deliveryStatus(NotificationDeliveryStatus.SENT)
        );

        List<ParticipantTaskDao.EnrolleeWithTasks> tasksRecentNotification = participantTaskDao.findByStatusAndTime(studyEnv.getId(), newTask1.getTaskType(),
                Duration.ofSeconds(0), Duration.ofHours(1), Duration.ofSeconds(1000), List.of(TaskStatus.NEW));
        assertThat(tasksRecentNotification, hasSize(0));
        List<ParticipantTaskDao.EnrolleeWithTasks> tasksAfterStaleNotification = participantTaskDao.findByStatusAndTime(studyEnv.getId(), newTask1.getTaskType(),
                Duration.ofSeconds(0), Duration.ofHours(1), Duration.ofSeconds(-10), List.of(TaskStatus.NEW));
        assertThat(tasksAfterStaleNotification, hasSize(1));
    }

    @Test
    @Transactional
    public void testFindByStatusAndTimeMultiTasks(TestInfo info) {
        PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted(getTestName(info));
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(portalEnv, getTestName(info));
        EnrolleeBundle enrolleeBundle = enrolleeFactory.buildWithPortalUser(getTestName(info), portalEnv, studyEnv);
        EnrolleeBundle enrolleeBundle2 = enrolleeFactory.buildWithPortalUser(getTestName(info), portalEnv, studyEnv);

        ParticipantTask task1_1 = participantTaskFactory.buildPersisted(enrolleeBundle, TaskStatus.NEW, TaskType.CONSENT);
        ParticipantTask task1_2 = participantTaskFactory.buildPersisted(enrolleeBundle, TaskStatus.NEW, TaskType.CONSENT);
        ParticipantTask task1_3 = participantTaskFactory.buildPersisted(enrolleeBundle, TaskStatus.COMPLETE, TaskType.CONSENT);
        ParticipantTask task2_1 = participantTaskFactory.buildPersisted(enrolleeBundle2, TaskStatus.NEW, TaskType.CONSENT);

        List<ParticipantTaskDao.EnrolleeWithTasks> enrolleeTasks = participantTaskDao.findByStatusAndTime(studyEnv.getId(), task1_1.getTaskType(),
                Duration.ofSeconds(0), Duration.ofHours(1), Duration.ofSeconds(1), List.of(TaskStatus.NEW));
        // should contain both enrollees
        assertThat(enrolleeTasks, hasSize(2));
        ParticipantTaskDao.EnrolleeWithTasks enrollee1tasks = enrolleeTasks.stream().filter(et ->
                et.getEnrolleeId().equals(enrolleeBundle.enrollee().getId())).findFirst().get();
        assertThat(enrollee1tasks.getTaskTargetNames(), hasSize(2));
        assertThat(enrollee1tasks.getTaskTargetNames(), containsInAnyOrder(task1_1.getTargetName(), task1_2.getTargetName()));

        // Now check that it filters out tasks if there is a recent notification
        Trigger trigger = triggerFactory.buildPersisted(Trigger.builder()
                        .deliveryType(NotificationDeliveryType.EMAIL)
                        .triggerType(TriggerType.TASK_REMINDER),
                studyEnv.getId(), portalEnv.getId());
        notificationFactory.buildPersisted(
                notificationFactory.builder(enrolleeBundle, trigger).deliveryStatus(NotificationDeliveryStatus.SENT)
        );

        List<ParticipantTaskDao.EnrolleeWithTasks> tasksRecentNotification = participantTaskDao.findByStatusAndTime(studyEnv.getId(), task1_1.getTaskType(),
                Duration.ofSeconds(0), Duration.ofHours(1), Duration.ofSeconds(1000), List.of(TaskStatus.NEW));
        assertThat(tasksRecentNotification, hasSize(1));
        assertThat(tasksRecentNotification.get(0).getEnrolleeId(), equalTo(enrolleeBundle2.enrollee().getId())); // only the second enrollee's task should appear
    }

    @Test
    @Transactional
    void testFindTasksByStudy(TestInfo testInfo) {
        String testName = getTestName(testInfo);
        PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted(testName);
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(portalEnv, testName);
        EnrolleeBundle enrolleeBundle = enrolleeFactory.buildWithPortalUser(testName, portalEnv, studyEnv);
        EnrolleeBundle enrolleeBundle2 = enrolleeFactory.buildWithPortalUser(testName, portalEnv, studyEnv);

        participantTaskFactory.buildPersisted(enrolleeBundle, "stable_id_1", "task_name1", TaskStatus.NEW, TaskType.CONSENT);
        participantTaskFactory.buildPersisted(enrolleeBundle, "stable_id_2", "task_name2", TaskStatus.NEW, TaskType.SURVEY);
        participantTaskFactory.buildPersisted(enrolleeBundle, "stable_id_3", "task_name3", TaskStatus.COMPLETE, TaskType.CONSENT);
        participantTaskFactory.buildPersisted(enrolleeBundle2, "stable_id_1", "task_name1", TaskStatus.COMPLETE, TaskType.SURVEY);

        List<ParticipantTaskDao.EnrolleeTasks> enrolleeTasks = participantTaskDao.findTaskNamesByStudy(studyEnv.getId());
        assertThat(enrolleeTasks, hasSize(3));
        assertThat(enrolleeTasks.stream().map(ParticipantTaskDao.EnrolleeTasks::getTargetStableId).toList(),
                containsInAnyOrder("stable_id_1", "stable_id_2", "stable_id_3"));
        assertThat(enrolleeTasks.stream().map(ParticipantTaskDao.EnrolleeTasks::getTargetName).toList(),
                containsInAnyOrder("task_name1", "task_name2", "task_name3"));
    }

    @Autowired
    private ParticipantTaskDao participantTaskDao;
    @Autowired
    private ParticipantTaskFactory participantTaskFactory;
    @Autowired
    private StudyEnvironmentFactory studyEnvironmentFactory;
    @Autowired
    private PortalEnvironmentFactory portalEnvironmentFactory;
    @Autowired
    private ParticipantTaskService participantTaskService;
    @Autowired
    private TriggerFactory triggerFactory;
    @Autowired
    private NotificationFactory notificationFactory;
    @Autowired
    private EnrolleeFactory enrolleeFactory;


}
