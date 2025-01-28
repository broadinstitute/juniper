package bio.terra.pearl.core.service.workflow;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.kit.KitRequestFactory;
import bio.terra.pearl.core.factory.kit.KitTypeFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeBundle;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.factory.portal.PortalEnvironmentFactory;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.kit.KitRequestStatus;
import bio.terra.pearl.core.model.kit.KitType;
import bio.terra.pearl.core.model.notification.*;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.SurveyResponse;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import bio.terra.pearl.core.model.workflow.TaskType;
import bio.terra.pearl.core.service.kit.pepper.PepperKitStatus;
import bio.terra.pearl.core.service.notification.NotificationService;
import bio.terra.pearl.core.service.notification.TriggerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class TriggerActionServiceTests extends BaseSpringBootTest {
    @Autowired
    private EnrolleeFactory enrolleeFactory;
    @Autowired
    private TriggerService triggerService;
    @Autowired
    private ParticipantTaskService participantTaskService;
    @Autowired
    private StudyEnvironmentFactory studyEnvironmentFactory;
    @Autowired
    private PortalEnvironmentFactory portalEnvironmentFactory;
    @Autowired
    private EventService eventService;
    @Autowired
    private KitTypeFactory kitTypeFactory;
    @Autowired
    private KitRequestFactory kitRequestFactory;
    @Autowired
    private NotificationService notificationService;

    @Test
    @Transactional
    public void testUpdateTaskStatus(TestInfo testInfo) {
        // simulate updating an survey task on completion of a kit request
        EnrolleeBundle enrolleeBundle = enrolleeFactory
                .buildWithPortalUser(getTestName(testInfo));
        ParticipantTask task = createTask(enrolleeBundle, "exampleTask", TaskStatus.NEW);
        ParticipantTask otherTask = createTask(enrolleeBundle, "otherTask", TaskStatus.NEW);
        EnrolleeBundle otherEnrollee = enrolleeFactory
                .buildWithPortalUser(getTestName(testInfo));
        ParticipantTask otherEnrolleeTask = createTask(otherEnrollee, "exampleTask", TaskStatus.NEW);

        // simulate updating an survey task on completion of a kit request
        Trigger config = createStatusTrigger(enrolleeBundle, TriggerEventType.KIT_SENT);
        config.setStatusToUpdateTo(TaskStatus.COMPLETE);
        config.setActionTargetStableIds(List.of("exampleTask"));
        triggerService.update(config);

        KitRequest kitRequest = createKitRequest(enrolleeBundle, getTestName(testInfo));
        eventService.publishKitStatusEvent(kitRequest, enrolleeBundle.enrollee(), enrolleeBundle.portalParticipantUser(),
                KitRequestStatus.SENT);

        // target task should be updated, other tasks should not
        task = participantTaskService.find(task.getId()).orElseThrow();
        assertThat(task.getStatus(), equalTo(TaskStatus.COMPLETE));
        otherTask = participantTaskService.find(otherTask.getId()).orElseThrow();
        assertThat(otherTask.getStatus(), equalTo(TaskStatus.NEW));
        otherEnrolleeTask = participantTaskService.find(otherEnrolleeTask.getId()).orElseThrow();
        assertThat(otherEnrolleeTask.getStatus(), equalTo(TaskStatus.NEW));
    }

    @Test
    @Transactional
    public void testTargetStableIdFilter(TestInfo testInfo) {
        EnrolleeBundle enrolleeBundle = enrolleeFactory
                .buildWithPortalUser(getTestName(testInfo));
        ParticipantTask targetTask = createTask(enrolleeBundle, "targetedSurvey", TaskStatus.NEW);
        ParticipantTask otherTask = createTask(enrolleeBundle, "otherSurvey", TaskStatus.NEW);
        ParticipantTask updateTask = createTask(enrolleeBundle, "updateSurvey", TaskStatus.NEW);
        EnrolleeBundle otherEnrollee = enrolleeFactory
                .buildWithPortalUser(getTestName(testInfo));
        ParticipantTask otherOtherTask = createTask(otherEnrollee, "otherSurvey", TaskStatus.NEW);
        ParticipantTask otherUpdateTask = createTask(otherEnrollee, "updateSurvey", TaskStatus.NEW);

        // simulate updating an survey task on completion of another survey
        Trigger config = createStatusTrigger(enrolleeBundle, TriggerEventType.SURVEY_RESPONSE);
        config.setStatusToUpdateTo(TaskStatus.COMPLETE);
        config.setFilterTargetStableIds(List.of("targetedSurvey"));
        config.setActionTargetStableIds(List.of("updateSurvey"));
        triggerService.update(config);

        // confirm that a survey event on the other survey does not trigger the status change
        eventService.publishEnrolleeSurveyEvent(enrolleeBundle.enrollee(), new SurveyResponse(),
                enrolleeBundle.portalParticipantUser(),
                otherTask);
        updateTask = participantTaskService.find(updateTask.getId()).orElseThrow();
        assertThat(updateTask.getStatus(), equalTo(TaskStatus.NEW));

        // confirm that a survey event on the targeted survey does trigger the status change
        eventService.publishEnrolleeSurveyEvent(enrolleeBundle.enrollee(), new SurveyResponse(),
                enrolleeBundle.portalParticipantUser(), targetTask);
        updateTask = participantTaskService.find(updateTask.getId()).orElseThrow();
        assertThat(updateTask.getStatus(), equalTo(TaskStatus.COMPLETE));

        // confirm the other enrollee's task is not updated
        otherUpdateTask = participantTaskService.find(otherUpdateTask.getId()).orElseThrow();
        assertThat(otherUpdateTask.getStatus(), equalTo(TaskStatus.NEW));
    }

    @Test
    @Transactional
    public void testKitNotifications(TestInfo testInfo) {
        String testName = getTestName(testInfo);
        KitType bloodKitType = kitTypeFactory.buildPersisted("Blood");
        PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted(testName);
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(portalEnv, testName);
        EnrolleeBundle enrolleeBundle = enrolleeFactory.buildWithPortalUser(testName, portalEnv, studyEnv, true);
        KitRequest kitRequest1 = kitRequestFactory.buildPersisted(testName, enrolleeBundle.enrollee(), PepperKitStatus.SENT, bloodKitType.getId());
        Trigger config = Trigger.builder()
                .triggerType(TriggerType.EVENT)
                .eventType(TriggerEventType.KIT_SENT)
                .actionType(TriggerActionType.NOTIFICATION)
                .deliveryType(NotificationDeliveryType.EMAIL)
                .studyEnvironmentId(studyEnv.getId())
                .portalEnvironmentId(portalEnv.getId())
                .build();
        config = triggerService.create(config);


        eventService.publishKitStatusEvent(kitRequest1, enrolleeBundle.enrollee(),
                enrolleeBundle.portalParticipantUser(),
                KitRequestStatus.CREATED);

        // confirm that a kit task got created
        List<ParticipantTask> tasks = participantTaskService.findByEnrolleeId(enrolleeBundle.enrollee().getId());
        assertThat(tasks.size(), equalTo(1));
        assertThat(tasks.get(0).getKitRequestId(), equalTo(kitRequest1.getId()));

        // confirm that a notification got sent
        List<Notification> notifications = notificationService.findByEnrolleeId(enrolleeBundle.enrollee().getId());
        assertThat(notifications.size(), equalTo(1));
        assertThat(notifications.get(0).getTriggerId(), equalTo(config.getId()));
    }

    @Test
    @Transactional
    public void testKitTypeNotifications(TestInfo testInfo) {
        String testName = getTestName(testInfo);
        KitType bloodKitType = kitTypeFactory.buildPersisted("Blood");
        KitType salivaKitType = kitTypeFactory.buildPersisted("Saliva");
        PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted(testName);
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(portalEnv, testName);
        EnrolleeBundle enrolleeBundle1 = enrolleeFactory.buildWithPortalUser(testName, portalEnv, studyEnv, true);
        EnrolleeBundle enrolleeBundle2 = enrolleeFactory.buildWithPortalUser(testName, portalEnv, studyEnv, true);

        KitRequest kitRequest1 = kitRequestFactory.buildPersisted(testName, enrolleeBundle1.enrollee(), PepperKitStatus.SENT, bloodKitType.getId());
        KitRequest kitRequest2 = kitRequestFactory.buildPersisted(testName, enrolleeBundle2.enrollee(), PepperKitStatus.SENT, salivaKitType.getId());


        Trigger bloodConfig = Trigger.builder()
                .triggerType(TriggerType.EVENT)
                .eventType(TriggerEventType.KIT_SENT)
                .actionType(TriggerActionType.NOTIFICATION)
                .filterTargetStableIds(List.of(bloodKitType.getName()))
                .deliveryType(NotificationDeliveryType.EMAIL)
                .studyEnvironmentId(studyEnv.getId())
                .portalEnvironmentId(portalEnv.getId())
                .build();
        bloodConfig = triggerService.create(bloodConfig);
        Trigger salivaConfig = Trigger.builder()
                .triggerType(TriggerType.EVENT)
                .eventType(TriggerEventType.KIT_SENT)
                .filterTargetStableIds(List.of(salivaKitType.getName()))
                .actionType(TriggerActionType.NOTIFICATION)
                .deliveryType(NotificationDeliveryType.EMAIL)
                .studyEnvironmentId(studyEnv.getId())
                .portalEnvironmentId(portalEnv.getId())
                .build();
        salivaConfig = triggerService.create(salivaConfig);


        eventService.publishKitStatusEvent(kitRequest1, enrolleeBundle1.enrollee(),
                enrolleeBundle1.portalParticipantUser(),
                KitRequestStatus.CREATED);
        eventService.publishKitStatusEvent(kitRequest2, enrolleeBundle2.enrollee(),
                enrolleeBundle2.portalParticipantUser(),
                KitRequestStatus.CREATED);

        // confirm that the correct notification got sent to each
        List<Notification> notifications = notificationService.findByEnrolleeId(enrolleeBundle1.enrollee().getId());
        assertThat(notifications.get(0).getTriggerId(), equalTo(bloodConfig.getId()));

        notifications = notificationService.findByEnrolleeId(enrolleeBundle2.enrollee().getId());
        assertThat(notifications.get(0).getTriggerId(), equalTo(salivaConfig.getId()));

    }

    private ParticipantTask createTask(EnrolleeBundle enrolleeBundle, String taskStableId, TaskStatus status ) {
        Enrollee enrollee = enrolleeBundle.enrollee();
        ParticipantTask task = ParticipantTask.builder()
                .enrolleeId(enrollee.getId())
                .studyEnvironmentId(enrollee.getStudyEnvironmentId())
                .portalParticipantUserId(enrolleeBundle.portalParticipantUser().getId())
                .targetStableId(taskStableId)
                .taskType(TaskType.SURVEY)
                .status(status)
                .build();
        task = participantTaskService.create(task, null);
        return task;
    }

    private Trigger createStatusTrigger(EnrolleeBundle enrolleeBundle, TriggerEventType eventType) {
        Enrollee enrollee = enrolleeBundle.enrollee();
        Trigger config = Trigger.builder()
                .studyEnvironmentId(enrollee.getStudyEnvironmentId())
                .eventType(eventType)
                .triggerType(TriggerType.EVENT)
                .actionType(TriggerActionType.TASK_STATUS_CHANGE)
                .portalEnvironmentId(enrolleeBundle.portalParticipantUser().getPortalEnvironmentId())
                .build();
        config = triggerService.create(config);
        return config;
    }

    private KitRequest createKitRequest(EnrolleeBundle enrolleeBundle, String testName) {
        KitType kitType = kitTypeFactory.buildPersisted(testName);
        KitRequest kitRequest = KitRequest.builder()
                .status(KitRequestStatus.SENT)
                .enrolleeId(enrolleeBundle.enrollee().getId())
                .kitType(kitType)
                .kitTypeId(kitType.getId())
                .build();
        return kitRequest;
    }


}
