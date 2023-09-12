package bio.terra.pearl.core.service.kit;

import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;

@Getter @Setter
@SuperBuilder @NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString
public class PepperErrorResponse extends PepperResponse {
    @NotNull
    private String errorMessage;
    @NotNull
    private String juniperKitId;
    private String value;
}
