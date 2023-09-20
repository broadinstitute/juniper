package bio.terra.pearl.core.service.kit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter @Setter
@SuperBuilder @NoArgsConstructor
@EqualsAndHashCode
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class PepperKitStatus {
    private String juniperKitId;
    private String currentStatus;
    private String dsmShippingLabel;
    @JsonProperty("participantId")
    private String dsmParticipantId;
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
