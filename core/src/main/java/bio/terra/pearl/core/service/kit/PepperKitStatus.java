package bio.terra.pearl.core.service.kit;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter @Setter
@SuperBuilder @NoArgsConstructor
@EqualsAndHashCode
@ToString
public class PepperKitStatus {
    private String kitId;
    private String currentStatus;
    private String labelDate;
    private String scanDate;
    private String receiveDate;
    private String trackingNumber;
    private String returnTrackingNumber;
    private String errorMessage;
    private String errorDate;
}
