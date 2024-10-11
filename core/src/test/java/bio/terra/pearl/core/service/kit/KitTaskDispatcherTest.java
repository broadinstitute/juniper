package bio.terra.pearl.core.service.kit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.participant.EnrolleeBundle;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.kit.KitRequestStatus;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import bio.terra.pearl.core.model.workflow.TaskType;
import bio.terra.pearl.core.service.workflow.ParticipantTaskService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

class KitTaskDispatcherTest extends BaseSpringBootTest {

    @Autowired
    private EnrolleeFactory enrolleeFactory;
    @MockBean
    private ParticipantTaskService mockTaskService;

    @Test
    @Transactional
    void testKitSentEventHandler(TestInfo testInfo) {
        String testName = getTestName(testInfo);
        EnrolleeBundle enrolleeBundle = enrolleeFactory.buildWithPortalUser(testName);

        KitRequest kitRequest = buildKitRequest(enrolleeBundle);
        UUID kitRequestId = kitRequest.getId();
        when(mockTaskService.findByKitRequestId(kitRequestId)).thenReturn(Optional.empty());

        when(mockTaskService.create(any(ParticipantTask.class), any(DataAuditInfo.class))).thenAnswer(invocation -> {
            ParticipantTask task = (ParticipantTask) invocation.getArguments()[0];
            assertThat(task.getStatus(), equalTo(TaskStatus.NEW));
            verifyTask(task, enrolleeBundle, kitRequestId);
            return null;
        });

        KitSentEvent kitEvent = KitSentEvent.builder()
                .enrollee(enrolleeBundle.enrollee())
                .priorStatus(KitRequestStatus.CREATED)
                .kitRequest(kitRequest)
                .portalParticipantUser(enrolleeBundle.portalParticipantUser()).build();

        KitTaskDispatcher taskDispatcher = new KitTaskDispatcher(mockTaskService);
        taskDispatcher.handleEvent(kitEvent);
        verify(mockTaskService).create(any(ParticipantTask.class), any(DataAuditInfo.class));
    }

    @Test
    @Transactional
    void testKitReceivedEventHandler(TestInfo testInfo) {
        String testName = getTestName(testInfo);
        EnrolleeBundle enrolleeBundle = enrolleeFactory.buildWithPortalUser(testName);

        KitRequest kitRequest = buildKitRequest(enrolleeBundle);
        UUID kitRequestId = kitRequest.getId();
        ParticipantTask participantTask = KitTaskDispatcher.buildTask(enrolleeBundle.enrollee(), kitRequest,
                enrolleeBundle.portalParticipantUser().getId());
        when(mockTaskService.findByKitRequestId(kitRequestId)).thenReturn(Optional.of(participantTask));

        when(mockTaskService.update(any(ParticipantTask.class), any(DataAuditInfo.class))).thenAnswer(invocation -> {
            ParticipantTask task = (ParticipantTask) invocation.getArguments()[0];
            assertThat(task.getStatus(), equalTo(TaskStatus.COMPLETE));
            verifyTask(task, enrolleeBundle, kitRequestId);
            return null;
        });

        kitRequest.setStatus(KitRequestStatus.RECEIVED);
        KitReceivedEvent kitEvent = KitReceivedEvent.builder()
                .enrollee(enrolleeBundle.enrollee())
                .priorStatus(KitRequestStatus.SENT)
                .kitRequest(kitRequest)
                .portalParticipantUser(enrolleeBundle.portalParticipantUser()).build();

        KitTaskDispatcher taskDispatcher = new KitTaskDispatcher(mockTaskService);
        taskDispatcher.handleEvent(kitEvent);
        verify(mockTaskService).update(any(ParticipantTask.class), any(DataAuditInfo.class));
    }

    @Test
    @Transactional
    void testKitSentEventHandlerWithReset(TestInfo testInfo) {
        String testName = getTestName(testInfo);
        EnrolleeBundle enrolleeBundle = enrolleeFactory.buildWithPortalUser(testName);

        KitRequest kitRequest = buildKitRequest(enrolleeBundle);
        UUID kitRequestId = kitRequest.getId();
        ParticipantTask participantTask = KitTaskDispatcher.buildTask(enrolleeBundle.enrollee(), kitRequest,
                enrolleeBundle.portalParticipantUser().getId());
        when(mockTaskService.findByKitRequestId(kitRequestId)).thenReturn(Optional.of(participantTask));

        when(mockTaskService.update(any(ParticipantTask.class), any(DataAuditInfo.class))).thenAnswer(invocation -> {
            ParticipantTask task = (ParticipantTask) invocation.getArguments()[0];
            assertThat(task.getStatus(), equalTo(TaskStatus.NEW));
            verifyTask(task, enrolleeBundle, kitRequestId);
            return null;
        });

        KitSentEvent kitEvent = KitSentEvent.builder()
                .enrollee(enrolleeBundle.enrollee())
                .priorStatus(KitRequestStatus.CREATED)
                .kitRequest(kitRequest)
                .portalParticipantUser(enrolleeBundle.portalParticipantUser()).build();

        KitTaskDispatcher taskDispatcher = new KitTaskDispatcher(mockTaskService);
        taskDispatcher.handleEvent(kitEvent);
        verify(mockTaskService).update(any(ParticipantTask.class), any(DataAuditInfo.class));
    }

    private KitRequest buildKitRequest(EnrolleeBundle enrolleeBundle) {
        UUID kitRequestId = UUID.randomUUID();
        return KitRequest.builder()
                .id(kitRequestId)
                .status(KitRequestStatus.SENT)
                .enrolleeId(enrolleeBundle.enrollee().getId())
                .build();
    }

    private void verifyTask(ParticipantTask task, EnrolleeBundle enrolleeBundle, UUID kitRequestId) {
        assertThat(task.getTaskType(), equalTo(TaskType.KIT_REQUEST));
        assertThat(task.getTargetName(), equalTo("Kit Request"));
        assertThat(task.getTargetStableId(), equalTo("kit_request"));
        assertThat(task.getEnrolleeId(), equalTo(enrolleeBundle.enrollee().getId()));
        assertThat(task.getKitRequestId(), equalTo(kitRequestId));
        assertThat(task.getPortalParticipantUserId(), equalTo(enrolleeBundle.portalParticipantUser().getId()));
        assertThat(task.getStudyEnvironmentId(), equalTo(enrolleeBundle.enrollee().getStudyEnvironmentId()));
    }
}
