package bio.terra.pearl.core.service.kit;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter @Setter
@SuperBuilder @NoArgsConstructor
@EqualsAndHashCode
@ToString
public class PepperErrorResponse {
    private String errorMessage;
    private String juniperKitId;
    private String value;
}
