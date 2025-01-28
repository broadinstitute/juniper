package bio.terra.pearl.core.service.notification;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.dao.dataimport.TimeShiftDao;
import bio.terra.pearl.core.dao.notification.NotificationDao;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeBundle;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.factory.participant.ParticipantTaskFactory;
import bio.terra.pearl.core.factory.portal.PortalEnvironmentFactory;
import bio.terra.pearl.core.factory.survey.SurveyFactory;
import bio.terra.pearl.core.model.notification.Notification;
import bio.terra.pearl.core.model.notification.NotificationDeliveryType;
import bio.terra.pearl.core.model.notification.Trigger;
import bio.terra.pearl.core.model.notification.TriggerType;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import bio.terra.pearl.core.model.workflow.TaskType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

class EnrolleeLaunchServiceTest extends BaseSpringBootTest {

    @Autowired
    private EnrolleeLaunchService enrolleeLaunchService;
    @Autowired
    private NotificationDao notificationDao;
    @Autowired
    private TriggerService triggerService;
    @Autowired
    private EnrolleeReminderService enrolleeReminderService;
    @Autowired
    private ParticipantTaskFactory participantTaskFactory;
    @Autowired
    private PortalEnvironmentFactory portalEnvironmentFactory;
    @Autowired
    private StudyEnvironmentFactory studyEnvironmentFactory;
    @Autowired
    private EnrolleeFactory enrolleeFactory;
    @Autowired
    private SurveyFactory surveyFactory;
    @Autowired
    private TimeShiftDao timeShiftDao;





    @Test
    @Transactional
    public void sendsTaskLaunchEmailsForRecentlyPublishedSurvey(TestInfo info) {
        PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted(getTestName(info));
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(portalEnv, getTestName(info));


        Survey survey = surveyFactory.buildPersisted(getTestName(info), portalEnv.getPortalId());
        surveyFactory.attachToEnv(survey, studyEnv.getId(), true);

        EnrolleeBundle enrolleeBundle = enrolleeFactory.buildWithPortalUser(getTestName(info), portalEnv, studyEnv, true);

        participantTaskFactory.buildPersisted(enrolleeBundle, survey.getStableId(), TaskStatus.NEW, TaskType.SURVEY);

        Trigger config = Trigger.builder()
                .triggerType(TriggerType.LAUNCH)
                .taskType(TaskType.SURVEY)
                .reminderIntervalMinutes(24 * 7 * 60) // send launch to any enrollees who get this task for 1 week
                .deliveryType(NotificationDeliveryType.EMAIL)
                .studyEnvironmentId(studyEnv.getId())
                .portalEnvironmentId(portalEnv.getId())
                .filterTargetStableIds(List.of(survey.getStableId()))
                .build();

        Trigger savedConfig = triggerService.create(config);
        enrolleeLaunchService.sendTaskLaunchEmails(studyEnv);

        List<Notification> notificationList = notificationDao.findByEnrolleeId(enrolleeBundle.enrollee().getId());
        assertThat(notificationList, hasSize(1));
        assertThat(notificationList.get(0).getTriggerId(), equalTo(savedConfig.getId()));

        enrolleeLaunchService.sendTaskLaunchEmails(studyEnv);
        // doesn't send a second email
        notificationList = notificationDao.findByEnrolleeId(enrolleeBundle.enrollee().getId());
        assertThat(notificationList, hasSize(1));

    }

    @Test
    @Transactional
    public void doesNotSendTaskLaunchEmailForOldSurvey(TestInfo info) {

        PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted(getTestName(info));
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(portalEnv, getTestName(info));


        Survey survey = surveyFactory.buildPersisted(getTestName(info), portalEnv.getPortalId());
        surveyFactory.attachToEnv(survey, studyEnv.getId(), true);

        EnrolleeBundle oldEnrolleeBundle = enrolleeFactory.buildWithPortalUser(getTestName(info), portalEnv, studyEnv, true);

        ParticipantTask oldTask = participantTaskFactory.buildPersisted(oldEnrolleeBundle, survey.getStableId(), TaskStatus.NEW, TaskType.SURVEY);

        // change the old task creation time to be 8 days ago, so no more launch emails will be sent.
        timeShiftDao.changeTasksCreationTime(List.of(oldTask.getId()), Instant.now().minus(Duration.ofDays(8)));

        EnrolleeBundle enrolleeBundle = enrolleeFactory.buildWithPortalUser(getTestName(info), portalEnv, studyEnv, true);
        participantTaskFactory.buildPersisted(enrolleeBundle, survey.getStableId(), TaskStatus.NEW, TaskType.SURVEY);


        Trigger config = Trigger.builder()
                .triggerType(TriggerType.LAUNCH)
                .taskType(TaskType.SURVEY)
                .reminderIntervalMinutes(24 * 7 * 60) // send launch to any enrollees who get this task for 1 week after first assignment
                .deliveryType(NotificationDeliveryType.EMAIL)
                .studyEnvironmentId(studyEnv.getId())
                .portalEnvironmentId(portalEnv.getId())
                .filterTargetStableIds(List.of(survey.getStableId()))
                .build();

        triggerService.create(config);
        enrolleeLaunchService.sendTaskLaunchEmails(studyEnv);

        List<Notification> notificationList = notificationDao.findByEnrolleeId(enrolleeBundle.enrollee().getId());
        assertThat(notificationList, hasSize(0));

        notificationList = notificationDao.findByEnrolleeId(oldEnrolleeBundle.enrollee().getId());
        assertThat(notificationList, hasSize(0));
    }
}
