package bio.terra.pearl.core.dao.participant;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.PortalEnvironmentFactory;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.notification.NotificationConfigFactory;
import bio.terra.pearl.core.factory.notification.NotificationFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.factory.participant.ParticipantUserFactory;
import bio.terra.pearl.core.model.notification.NotificationConfig;
import bio.terra.pearl.core.model.notification.NotificationDeliveryType;
import bio.terra.pearl.core.model.notification.NotificationType;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import bio.terra.pearl.core.model.workflow.TaskType;
import bio.terra.pearl.core.service.notification.NotificationService;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.ParticipantTaskService;
import java.time.Duration;
import java.util.List;
import org.apache.commons.lang3.RandomStringUtils;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
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
        ParticipantTask newTask1 = participantTaskService.create(taskForUser(enrolleeBundle, TaskStatus.NEW).build());

        // check status filtering
        var tasks = participantTaskDao.findByStatusAndTime(studyEnv.getId(), Duration.ofSeconds(0),
                Duration.ofSeconds(1), List.of(TaskStatus.NEW));
        assertThat(tasks, hasSize(1));

        var inProgressTasks = participantTaskDao.findByStatusAndTime(studyEnv.getId(), Duration.ofSeconds(0),
                Duration.ofSeconds(1), List.of(TaskStatus.IN_PROGRESS));
        assertThat(inProgressTasks, hasSize(0));

        var inProgressAndNewTasks = participantTaskDao.findByStatusAndTime(studyEnv.getId(), Duration.ofSeconds(0),
                Duration.ofSeconds(1), List.of(TaskStatus.NEW, TaskStatus.IN_PROGRESS));
        assertThat(inProgressAndNewTasks, hasSize(1));

        var minsAgoTasks = participantTaskDao.findByStatusAndTime(studyEnv.getId(), Duration.ofMinutes(5),
                Duration.ofSeconds(1), List.of(TaskStatus.NEW));
        assertThat(minsAgoTasks, hasSize(0));


        // Now check that it filters out tasks if there is a recent notification
        var notificationConfig = notificationConfigFactory.buildPersisted(NotificationConfig.builder()
                .deliveryType(NotificationDeliveryType.EMAIL)
                        .notificationType(NotificationType.TASK),
                                studyEnv.getId(), portalEnv.getId());
        notificationFactory.buildPersisted(enrolleeBundle, notificationConfig);

        var tasksRecentNotification = participantTaskDao.findByStatusAndTime(studyEnv.getId(), Duration.ofSeconds(0),
                Duration.ofSeconds(1000), List.of(TaskStatus.NEW));
        assertThat(tasksRecentNotification, hasSize(0));
        var tasksAfterStaleNotification = participantTaskDao.findByStatusAndTime(studyEnv.getId(), Duration.ofSeconds(0),
                Duration.ofSeconds(-10), List.of(TaskStatus.NEW));
        assertThat(tasksAfterStaleNotification, hasSize(1));
    }

    @Test
    @Transactional
    public void testFindByStatusAndTimeMultiTasks() {
        PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted("testFindByStatusAndTimeMulti");
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(portalEnv, "testFindByStatusAndTimeMulti");
        var enrolleeBundle = enrolleeFactory.buildWithPortalUser("testFindByStatusAndTimeMulti", portalEnv, studyEnv);
        var enrolleeBundle2 = enrolleeFactory.buildWithPortalUser("testFindByStatusAndTimeMulti", portalEnv, studyEnv);

        ParticipantTask newTask1 = participantTaskService.create(taskForUser(enrolleeBundle, TaskStatus.NEW).build());
        ParticipantTask newTask2 = participantTaskService.create(taskForUser(enrolleeBundle, TaskStatus.COMPLETE).build());
        ParticipantTask newTask3 = participantTaskService.create(taskForUser(enrolleeBundle2, TaskStatus.NEW).build());

        // should
        List<ParticipantTask> tasks = participantTaskDao.findByStatusAndTime(studyEnv.getId(), Duration.ofSeconds(0),
                Duration.ofSeconds(1), List.of(TaskStatus.NEW));
        assertThat(tasks, hasSize(2));
        assertThat(tasks, contains(newTask1, newTask3));
    }

    private ParticipantTask.ParticipantTaskBuilder taskForUser(EnrolleeFactory.EnrolleeBundle enrolleeBundle, TaskStatus status) {
        return ParticipantTask.builder()
                .status(status)
                .enrolleeId(enrolleeBundle.enrollee().getId())
                .taskType(TaskType.CONSENT)
                .studyEnvironmentId(enrolleeBundle.enrollee().getStudyEnvironmentId())
                .targetName(RandomStringUtils.randomAlphabetic(6))
                .portalParticipantUserId(enrolleeBundle.portalParticipantUser().getId());
    }

    @Autowired
    private ParticipantTaskDao participantTaskDao;
    @Autowired
    private StudyEnvironmentFactory studyEnvironmentFactory;
    @Autowired
    private PortalEnvironmentFactory portalEnvironmentFactory;
    @Autowired
    private ParticipantUserFactory participantUserFactory;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private EnrolleeService enrolleeService;
    @Autowired
    private ParticipantTaskService participantTaskService;
    @Autowired
    private NotificationConfigFactory notificationConfigFactory;
    @Autowired
    private NotificationFactory notificationFactory;
    @Autowired
    private EnrolleeFactory enrolleeFactory;


}
