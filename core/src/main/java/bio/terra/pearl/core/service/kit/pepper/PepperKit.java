package bio.terra.pearl.core.service.kit.pepper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.experimental.SuperBuilder;

/** holds kit request data as it is received from Pepper */
@Getter @Setter
@SuperBuilder @NoArgsConstructor
@EqualsAndHashCode
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class PepperKit {
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
