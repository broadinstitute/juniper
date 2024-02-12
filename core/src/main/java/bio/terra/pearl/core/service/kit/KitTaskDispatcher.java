package bio.terra.pearl.core.service.kit;

import java.util.Optional;
import java.util.UUID;

import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import bio.terra.pearl.core.model.workflow.TaskType;
import bio.terra.pearl.core.service.workflow.DispatcherOrder;
import bio.terra.pearl.core.service.workflow.ParticipantTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KitTaskDispatcher {

    private final ParticipantTaskService participantTaskService;

    public KitTaskDispatcher(ParticipantTaskService participantTaskService) {
        this.participantTaskService = participantTaskService;
    }

    @EventListener
    @Order(DispatcherOrder.KIT_TASK)
    public void handleEvent(KitSentEvent kitSentEvent) {
        Enrollee enrollee = kitSentEvent.getEnrollee();
        UUID portalParticipantUserId = kitSentEvent.getPortalParticipantUser().getId();
        KitRequest kitRequest = kitSentEvent.getKitRequest();
        Optional<ParticipantTask> task = participantTaskService.findByKitRequestId(kitRequest.getId());
        DataAuditInfo auditInfo = DataAuditInfo.builder().enrolleeId(enrollee.getId())
                .portalParticipantUserId(portalParticipantUserId)
                .systemProcess(DataAuditInfo.systemProcessName(getClass(), ".handleSentEvent")).build();
        if (task.isPresent()) {
            resetTask(task.get(), enrollee, portalParticipantUserId, auditInfo);
            return;
        }
        participantTaskService.create(buildTask(enrollee, kitRequest, portalParticipantUserId), auditInfo);
        log.info("Created kit task for enrollee {} with kit request {}",
                enrollee.getShortcode(), kitRequest.getId());
    }


    @EventListener
    @Order(DispatcherOrder.KIT_TASK)
    public void handleEvent(KitReceivedEvent kitEvent) {
        Enrollee enrollee = kitEvent.getEnrollee();
        KitRequest kitRequest = kitEvent.getKitRequest();
        Optional<ParticipantTask> task = participantTaskService.findByKitRequestId(kitRequest.getId());
        if (task.isEmpty()) {
            log.warn("KitReceivedEvent: Kit task does not exist for enrollee {} with kit request {}",
                    enrollee.getShortcode(), kitEvent.getKitRequest().getId());
            return;
        }
        ParticipantTask participantTask = task.get();
        participantTask.setStatus(TaskStatus.COMPLETE);
        DataAuditInfo auditInfo = DataAuditInfo.builder().enrolleeId(enrollee.getId())
                .portalParticipantUserId(kitEvent.getPortalParticipantUser().getId())
                .systemProcess(DataAuditInfo.systemProcessName(getClass(), "handleReceivedEvent")).build();

        participantTaskService.update(participantTask, auditInfo);
    }

    protected static ParticipantTask buildTask(Enrollee enrollee, KitRequest kitRequest, UUID portalParticipantUserId) {
        return ParticipantTask.builder()
                .enrolleeId(enrollee.getId())
                .portalParticipantUserId(portalParticipantUserId)
                .studyEnvironmentId(enrollee.getStudyEnvironmentId())
                .blocksHub(false)
                .taskOrder(0) // for now, no particular order among kit tasks
                .taskType(TaskType.KIT_REQUEST)
                .targetName("Kit Request")
                .targetStableId("kit_request")
                .status(TaskStatus.NEW)
                .kitRequestId(kitRequest.getId())
                .build();
    }

    /**
     * There may be cases where an existing kit request tasks get reused. For example, if after shipping
     * a kit request is deactivated (perhaps by mistake) and then reactivated. (Also, the current sandbox
     * behavior may trigger multiple sent events for a single kit request.)
     * This method will verify the kit request data that should not change, log an error if it does, but
     * reset the task to NEW otherwise and update the DB.
     *
     * @param task task to reset
     * @param enrollee enrollee associated with the event
     * @param portalParticipantUserId portal participant user id associated with the event
     */
    protected void resetTask(ParticipantTask task, Enrollee enrollee, UUID portalParticipantUserId, DataAuditInfo auditInfo) {
        if (!(task.getEnrolleeId().equals(enrollee.getId())
                && task.getPortalParticipantUserId().equals(portalParticipantUserId))) {
            log.error("Kit task already exists for enrollee {} kit request {}",
                    enrollee.getShortcode(), task.getKitRequestId());
            return;
        }
        task.setStatus(TaskStatus.NEW);
        participantTaskService.update(task, auditInfo);
        log.warn("Reset task for enrollee {} kit request {}",
                enrollee.getShortcode(), task.getKitRequestId());
    }
}
