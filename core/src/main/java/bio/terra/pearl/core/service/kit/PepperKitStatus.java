package bio.terra.pearl.core.service.kit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Getter @Setter
@SuperBuilder @NoArgsConstructor
@EqualsAndHashCode
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class PepperKitStatus {

    public enum Status {
        CREATED("kit without label"),
        QUEUED("queue"),
        SENT("sent"),
        RECEIVED("received"),
        ERRORED("error"),
        DEACTIVATED("deactivated");

        Status(String currentStatus) {
            this.currentStatus = currentStatus;
        }

        final String currentStatus;

        public static Status fromCurrentStatus(String currentStatus) {
            for (Status status : Status.values()) {
                if (status.currentStatus.equals(currentStatus.toLowerCase())) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Unexpected Pepper currentStatus: " + currentStatus);
        }

        public static boolean isCompleted(Status status) {
            return Set.of(Status.RECEIVED, Status.DEACTIVATED).contains(status);
        }

        public static boolean isFailed(Status status) {
            return status == Status.ERRORED;
        }

    }

    private String juniperKitId;
    private String currentStatus;
    private String dsmShippingLabel;
    private String participantId; // Juniper enrollee shortcode
    private String labelDate;
    private String labelByEmail;
    private String scanDate;
    private String scanByEmail;
    private String receiveDate;
    private String trackingScanBy;
    private String trackingNumber;
    private String returnTrackingNumber;
    private String errorMessage;
    private String errorDate;
    private Boolean error;
}
