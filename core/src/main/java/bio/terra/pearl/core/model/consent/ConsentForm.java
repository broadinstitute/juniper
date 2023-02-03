package bio.terra.pearl.core.model.consent;

import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.Versioned;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/** See https://broadworkbench.atlassian.net/wiki/spaces/PEARL/pages/2669281289/Consent+forms */
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class ConsentForm extends BaseEntity implements Versioned {
    private String stableId;
    @Builder.Default
    private int version = 1;
    private String content;
    private String name;
    // used to keep forms attached to their portal even if they are not on an environment currently
    private UUID portalId;
}
