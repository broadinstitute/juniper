package bio.terra.pearl.core.service.kit;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Getter @Setter
@SuperBuilder @NoArgsConstructor
@EqualsAndHashCode
@ToString
public class PepperDSMKitStatus {
    private String kitId;
    private String currentStatus;
    private String errorMessage;
    private Instant errorDate;
}
