package bio.terra.pearl.core.service.kit.pepper;

import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Additional fields included in the request to DSM when requesting a kit.
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class PepperKitMetadata {
    private String sexAtBirth;
}

