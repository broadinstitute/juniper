package bio.terra.pearl.core.service.kit.pepper;

import bio.terra.pearl.core.model.kit.KitRequestStatus;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@Slf4j
public enum PepperKitStatus {
    CREATED("kit without label"),
    QUEUED("queue"),
    SENT("sent"),
    RECEIVED("received"),
    ERRORED("error"),
    DEACTIVATED("deactivated");


    PepperKitStatus(String pepperString) {
        this.pepperString = pepperString;
    }

    public final String pepperString;

    public static PepperKitStatus fromCurrentStatus(String currentStatus) {
        for (PepperKitStatus status : PepperKitStatus.values()) {
            if (status.pepperString.equals(currentStatus.toLowerCase())) {
                return status;
            }
        }
        return null;
    }

    public static KitRequestStatus mapToKitRequestStatus(String currentStatus) {
        PepperKitStatus pepperKitStatus = fromCurrentStatus(currentStatus);
        if (pepperKitStatus != null) {
            try {
                return KitRequestStatus.valueOf(pepperKitStatus.name());
            } catch (IllegalArgumentException e) {
                // log will be below
            }
        }
        log.error("Unknown PepperKitStatus: {}", currentStatus);
        return KitRequestStatus.UNKNOWN;
    }

    public static boolean isCompleted(PepperKitStatus status) {
        return Set.of(PepperKitStatus.RECEIVED, PepperKitStatus.DEACTIVATED).contains(status);
    }

    public static boolean isFailed(PepperKitStatus status) {
        return status == PepperKitStatus.ERRORED;
    }
}
