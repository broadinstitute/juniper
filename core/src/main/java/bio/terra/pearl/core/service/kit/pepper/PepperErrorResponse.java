package bio.terra.pearl.core.service.kit.pepper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;

@Getter @Setter
@SuperBuilder @NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class PepperErrorResponse extends PepperResponse {
    @NotNull
    private String errorMessage;
    @NotNull
    private String juniperKitId;
    private PepperErrorValue value;

    @Getter @Setter
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class PepperErrorValue {
        @NotNull
        private String detailMessage;
    }
}
