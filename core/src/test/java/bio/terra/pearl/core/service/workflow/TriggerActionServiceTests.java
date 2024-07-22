package bio.terra.pearl.core.service.workflow;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.kit.KitTypeFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.kit.KitRequestStatus;
import bio.terra.pearl.core.model.notification.*;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import bio.terra.pearl.core.model.workflow.TaskType;
import bio.terra.pearl.core.service.notification.TriggerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

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
    private EventService eventService;
    @Autowired
    private KitTypeFactory kitTypeFactory;

    @Test
    @Transactional
    public void testUpdateTaskStatus(TestInfo testInfo) {
        // simulate updating an survey task on completion of a kit request
        EnrolleeFactory.EnrolleeBundle enrolleeBundle = enrolleeFactory
                .buildWithPortalUser(getTestName(testInfo));
        ParticipantTask task = createTask(enrolleeBundle, "exampleTask", TaskStatus.NEW);

        Trigger config = createStatusTrigger(enrolleeBundle, TriggerEventType.KIT_SENT);
        config.setStatusToUpdateTo(TaskStatus.COMPLETE);
        config.setTaskTargetStableId("exampleTask");
        triggerService.update(config);

        KitRequest kitRequest = createKitRequest(enrolleeBundle, getTestName(testInfo));
        eventService.publishKitStatusEvent(kitRequest, enrolleeBundle.enrollee(), enrolleeBundle.portalParticipantUser(),
                KitRequestStatus.SENT);

        task = participantTaskService.find(task.getId()).orElseThrow();
        assertThat(task.getStatus(), equalTo(TaskStatus.COMPLETE));
    }

    private ParticipantTask createTask(EnrolleeFactory.EnrolleeBundle enrolleeBundle, String taskStableId, TaskStatus status ) {
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


    private Trigger createStatusTrigger(EnrolleeFactory.EnrolleeBundle enrolleeBundle, TriggerEventType eventType) {
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

    private KitRequest createKitRequest(EnrolleeFactory.EnrolleeBundle enrolleeBundle, String testName) {
        KitRequest kitRequest = KitRequest.builder()
                .status(KitRequestStatus.SENT)
                .enrolleeId(enrolleeBundle.enrollee().getId())
                .kitType(kitTypeFactory.buildPersisted(testName))
                .build();
        return kitRequest;
    }


}
