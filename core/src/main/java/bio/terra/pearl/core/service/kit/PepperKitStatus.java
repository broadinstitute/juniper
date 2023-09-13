package bio.terra.pearl.core.service.kit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
    private String labelDate;
    private String scanDate;
    private String receiveDate;
    private String trackingNumber;
    private String returnTrackingNumber;
    private String errorMessage;
    private String errorDate;
    private Boolean error;
}
