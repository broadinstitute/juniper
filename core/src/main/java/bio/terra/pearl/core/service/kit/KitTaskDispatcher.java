package bio.terra.pearl.core.service.kit;

import java.util.UUID;

import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.participant.Enrollee;
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
        KitRequest kitRequest = kitSentEvent.getKitRequest();
        if (participantTaskService.findByKitRequestId(kitRequest.getId()).isPresent()) {
            log.error("Kit task already exists for enrollee {} kit request {}",
                    enrollee.getShortcode(), kitSentEvent.getKitRequest().getId());
            return;
        }

        participantTaskService.create(buildTask(enrollee, kitRequest, kitSentEvent.getPortalParticipantUser().getId()));
        log.info("Created kit task for enrollee {} kit request {}",
                enrollee.getShortcode(), kitRequest.getId());
    }

/*
    @EventListener
    @Order(DispatcherOrder.KIT_TASK)
    public void handleEvent(KitReceivedEvent kitReceivedEvent) {

    }
*/
    protected ParticipantTask buildTask(Enrollee enrollee, KitRequest kitRequest, UUID portalParticipantUserId) {
        return ParticipantTask.builder()
                .enrolleeId(enrollee.getId())
                .portalParticipantUserId(portalParticipantUserId)
                .studyEnvironmentId(enrollee.getStudyEnvironmentId())
                .blocksHub(false)
                .taskOrder(0) // for now, no particular order among kit tasks
                .taskType(TaskType.KIT_REQUEST)
                .targetName("Return %s kit".formatted(kitRequest.getKitType().getName()))
                .status(TaskStatus.NEW)
                .kitRequestId(kitRequest.getId())
                .build();
    }
}
