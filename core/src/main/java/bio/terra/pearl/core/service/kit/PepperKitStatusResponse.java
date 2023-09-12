package bio.terra.pearl.core.service.kit;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString
public class PepperKitStatusResponse extends PepperResponse {
    private PepperKitStatus[] kits;
}
