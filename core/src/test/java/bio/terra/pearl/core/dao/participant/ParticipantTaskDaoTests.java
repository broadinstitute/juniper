package bio.terra.pearl.core.dao.participant;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.dao.workflow.ParticipantTaskDao;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.notification.NotificationConfigFactory;
import bio.terra.pearl.core.factory.notification.NotificationFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.factory.participant.ParticipantTaskFactory;
import bio.terra.pearl.core.factory.portal.PortalEnvironmentFactory;
import bio.terra.pearl.core.model.notification.TriggeredAction;
import bio.terra.pearl.core.model.notification.NotificationDeliveryStatus;
import bio.terra.pearl.core.model.notification.NotificationDeliveryType;
import bio.terra.pearl.core.model.notification.TriggerType;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import bio.terra.pearl.core.model.workflow.TaskType;
import bio.terra.pearl.core.service.workflow.ParticipantTaskService;
import java.time.Duration;
import java.util.List;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class ParticipantTaskDaoTests extends BaseSpringBootTest {
    @Test
    @Transactional
    public void testFindByStatusAndTimeOneTask() {
        PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted("testFindByStatusAndTime");
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(portalEnv, "testFindByStatusAndTime");
        var enrolleeBundle = enrolleeFactory.buildWithPortalUser("testFindByStatusAndTime", portalEnv, studyEnv);
        ParticipantTask newTask1 = participantTaskFactory.buildPersisted(enrolleeBundle, TaskStatus.NEW, TaskType.CONSENT);

        // check status filtering
        var tasks = participantTaskDao.findByStatusAndTime(studyEnv.getId(), newTask1.getTaskType(),
                Duration.ofSeconds(0), Duration.ofHours(1), Duration.ofSeconds(1), List.of(TaskStatus.NEW));
        assertThat(tasks, hasSize(1));

        var inProgressTasks = participantTaskDao.findByStatusAndTime(studyEnv.getId(), newTask1.getTaskType(),
                Duration.ofSeconds(0), Duration.ofHours(1), Duration.ofSeconds(1), List.of(TaskStatus.IN_PROGRESS));
        assertThat(inProgressTasks, hasSize(0));

        var inProgressAndNewTasks = participantTaskDao.findByStatusAndTime(studyEnv.getId(), newTask1.getTaskType(),
                Duration.ofSeconds(0), Duration.ofHours(1), Duration.ofSeconds(1), List.of(TaskStatus.NEW, TaskStatus.IN_PROGRESS));
        assertThat(inProgressAndNewTasks, hasSize(1));

        var minsAgoTasks = participantTaskDao.findByStatusAndTime(studyEnv.getId(), newTask1.getTaskType(),
                Duration.ofMinutes(5), Duration.ofHours(1), Duration.ofSeconds(1), List.of(TaskStatus.NEW));
        assertThat(minsAgoTasks, hasSize(0));


        // Now check that it filters out tasks if there is a recent notification
        var notificationConfig = notificationConfigFactory.buildPersisted(TriggeredAction.builder()
                .deliveryType(NotificationDeliveryType.EMAIL)
                .triggerType(TriggerType.TASK_REMINDER),
                                studyEnv.getId(), portalEnv.getId());
        notificationFactory.buildPersisted(
                notificationFactory.builder(enrolleeBundle, notificationConfig).deliveryStatus(NotificationDeliveryStatus.SENT)
        );

        var tasksRecentNotification = participantTaskDao.findByStatusAndTime(studyEnv.getId(), newTask1.getTaskType(),
                Duration.ofSeconds(0), Duration.ofHours(1), Duration.ofSeconds(1000), List.of(TaskStatus.NEW));
        assertThat(tasksRecentNotification, hasSize(0));
        var tasksAfterStaleNotification = participantTaskDao.findByStatusAndTime(studyEnv.getId(), newTask1.getTaskType(),
                Duration.ofSeconds(0), Duration.ofHours(1), Duration.ofSeconds(-10), List.of(TaskStatus.NEW));
        assertThat(tasksAfterStaleNotification, hasSize(1));
    }

    @Test
    @Transactional
    public void testFindByStatusAndTimeMultiTasks() {
        PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted("testFindByStatusAndTimeMulti");
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(portalEnv, "testFindByStatusAndTimeMulti");
        var enrolleeBundle = enrolleeFactory.buildWithPortalUser("testFindByStatusAndTimeMulti", portalEnv, studyEnv);
        var enrolleeBundle2 = enrolleeFactory.buildWithPortalUser("testFindByStatusAndTimeMulti", portalEnv, studyEnv);

        ParticipantTask task1_1 = participantTaskFactory.buildPersisted(enrolleeBundle, TaskStatus.NEW, TaskType.CONSENT);
        ParticipantTask task1_2 = participantTaskFactory.buildPersisted(enrolleeBundle, TaskStatus.NEW, TaskType.CONSENT);
        ParticipantTask task1_3 = participantTaskFactory.buildPersisted(enrolleeBundle, TaskStatus.COMPLETE, TaskType.CONSENT);
        ParticipantTask task2_1 = participantTaskFactory.buildPersisted(enrolleeBundle2, TaskStatus.NEW, TaskType.CONSENT);

        var enrolleeTasks = participantTaskDao.findByStatusAndTime(studyEnv.getId(), task1_1.getTaskType(),
                Duration.ofSeconds(0), Duration.ofHours(1), Duration.ofSeconds(1), List.of(TaskStatus.NEW));
        // should contain both enrollees
        assertThat(enrolleeTasks, hasSize(2));
        var enrollee1tasks = enrolleeTasks.stream().filter(et ->
                et.getEnrolleeId().equals(enrolleeBundle.enrollee().getId())).findFirst().get();
        assertThat(enrollee1tasks.getTaskTargetNames(), hasSize(2));
        assertThat(enrollee1tasks.getTaskTargetNames(), contains(task1_1.getTargetName(), task1_2.getTargetName()));

        // Now check that it filters out tasks if there is a recent notification
        var notificationConfig = notificationConfigFactory.buildPersisted(TriggeredAction.builder()
                        .deliveryType(NotificationDeliveryType.EMAIL)
                        .triggerType(TriggerType.TASK_REMINDER),
                studyEnv.getId(), portalEnv.getId());
        notificationFactory.buildPersisted(
                notificationFactory.builder(enrolleeBundle, notificationConfig).deliveryStatus(NotificationDeliveryStatus.SENT)
        );

        var tasksRecentNotification = participantTaskDao.findByStatusAndTime(studyEnv.getId(), task1_1.getTaskType(),
                Duration.ofSeconds(0), Duration.ofHours(1), Duration.ofSeconds(1000), List.of(TaskStatus.NEW));
        assertThat(tasksRecentNotification, hasSize(1));
        assertThat(tasksRecentNotification.get(0).getEnrolleeId(), equalTo(enrolleeBundle2.enrollee().getId())); // only the second enrollee's task should appear
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
    private NotificationConfigFactory notificationConfigFactory;
    @Autowired
    private NotificationFactory notificationFactory;
    @Autowired
    private EnrolleeFactory enrolleeFactory;


}
