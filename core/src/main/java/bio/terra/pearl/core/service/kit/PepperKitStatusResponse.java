package bio.terra.pearl.core.service.kit;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class PepperKitStatusResponse {
    private PepperKitStatus[] kits;
}
