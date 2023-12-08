package bio.terra.pearl.core.service.kit;

import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.kit.KitRequestStatus;
import bio.terra.pearl.core.service.workflow.EnrolleeEvent;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/** Event for when a change occurs to a kit status */
@Getter
@Setter
@SuperBuilder
public class KitStatusEvent extends EnrolleeEvent {
    private KitRequestStatus priorStatus;
    private KitRequest kitRequest;

    public static KitStatusEvent newInstance(KitRequest kitRequest, KitRequestStatus priorStatus) {
        if (KitRequestStatus.SENT.equals(kitRequest.getStatus())) {
            return KitSentEvent.builder()
                .kitRequest(kitRequest)
                .priorStatus(priorStatus)
                    .build();
        } else if (KitRequestStatus.RECEIVED.equals(kitRequest.getStatus())) {
            return KitReceivedEvent.builder()
                .kitRequest(kitRequest)
                .priorStatus(priorStatus)
                    .build();
        }
        return KitStatusEvent.builder()
            .kitRequest(kitRequest)
            .priorStatus(priorStatus)
                .build();
    }
}
