package bio.terra.pearl.core.dao.participant;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.PortalEnvironmentFactory;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.participant.ParticipantUserFactory;
import bio.terra.pearl.core.model.notification.NotificationConfig;
import bio.terra.pearl.core.model.notification.NotificationDeliveryType;
import bio.terra.pearl.core.model.notification.NotificationType;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import bio.terra.pearl.core.model.workflow.TaskType;
import bio.terra.pearl.core.service.notification.NotificationConfigService;
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
        var userBundle = participantUserFactory.buildPersisted(portalEnv,"testFindByStatusAndTime");
        Enrollee enrollee = enrolleeService.create(Enrollee.builder()
                .participantUserId(userBundle.ppUser().getParticipantUserId())
                .studyEnvironmentId(studyEnv.getId()).build());
        ParticipantTask newTask1 = participantTaskService.create(taskForUser(userBundle, enrollee, TaskStatus.NEW).build());

        // should
        List<ParticipantTask> tasks = participantTaskDao.findByStatusAndTime(studyEnv.getId(), Duration.ofSeconds(0),
                Duration.ofSeconds(1), List.of(TaskStatus.NEW));
        assertThat(tasks, hasSize(1));

        List<ParticipantTask> inProgressTasks = participantTaskDao.findByStatusAndTime(studyEnv.getId(), Duration.ofSeconds(0),
                Duration.ofSeconds(1), List.of(TaskStatus.IN_PROGRESS));
        assertThat(inProgressTasks, hasSize(0));

        List<ParticipantTask> inProgressAndNewTasks = participantTaskDao.findByStatusAndTime(studyEnv.getId(), Duration.ofSeconds(0),
                Duration.ofSeconds(1), List.of(TaskStatus.NEW, TaskStatus.IN_PROGRESS));
        assertThat(inProgressAndNewTasks, hasSize(1));

        List<ParticipantTask> minsAgoTasks = participantTaskDao.findByStatusAndTime(studyEnv.getId(), Duration.ofMinutes(5),
                Duration.ofSeconds(1), List.of(TaskStatus.NEW));
        assertThat(minsAgoTasks, hasSize(0));

        notificationConfigService.create(NotificationConfig.builder()
                .deliveryType(NotificationDeliveryType.EMAIL)
                        .notificationType(NotificationType.TASK)
                                .studyEnvironmentId(studyEnv.getId())
                                        .portalEnvironmentId(portalEnv.getId())
                                                .
        notificationService.create()
    }

    @Test
    @Transactional
    public void testFindByStatusAndTimeMultiTasks() {
        PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted("testFindByStatusAndTime");
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(portalEnv, "testFindByStatusAndTime");
        var userBundle1 = participantUserFactory.buildPersisted(portalEnv,"testFindByStatusAndTime");
        Enrollee enrollee1 = enrolleeService.create(Enrollee.builder()
                .participantUserId(userBundle1.ppUser().getParticipantUserId())
                .studyEnvironmentId(studyEnv.getId()).build());
        var userBundle2 = participantUserFactory.buildPersisted(portalEnv,"testFindByStatusAndTime");
        Enrollee enrollee2 = enrolleeService.create(Enrollee.builder()
                .participantUserId(userBundle2.ppUser().getParticipantUserId())
                .studyEnvironmentId(studyEnv.getId()).build());
        ParticipantTask newTask1 = participantTaskService.create(taskForUser(userBundle1, enrollee1, TaskStatus.NEW).build());
        ParticipantTask newTask2 = participantTaskService.create(taskForUser(userBundle1, enrollee1, TaskStatus.COMPLETE).build());
        ParticipantTask newTask3 = participantTaskService.create(taskForUser(userBundle2, enrollee2, TaskStatus.NEW).build());

        // should
        List<ParticipantTask> tasks = participantTaskDao.findByStatusAndTime(studyEnv.getId(), Duration.ofSeconds(0),
                Duration.ofSeconds(1), List.of(TaskStatus.NEW));
        assertThat(tasks, hasSize(2));
        assertThat(tasks, contains(newTask1, newTask3));
    }

    private ParticipantTask.ParticipantTaskBuilder taskForUser(ParticipantUserFactory.ParticipantUserAndPortalUser userBundle,
                                                               Enrollee enrollee, TaskStatus status) {
        return ParticipantTask.builder()
                .status(status)
                .enrolleeId(enrollee.getId())
                .taskType(TaskType.CONSENT)
                .studyEnvironmentId(enrollee.getStudyEnvironmentId())
                .targetName(RandomStringUtils.randomAlphabetic(6))
                .portalParticipantUserId(userBundle.ppUser().getId());
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
    private NotificationConfigService notificationConfigService;


}
