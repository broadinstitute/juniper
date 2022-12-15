package bio.terra.pearl.core.model.survey;

import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.Versioned;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter @Setter @NoArgsConstructor @SuperBuilder
public class Survey extends BaseEntity implements Versioned {
    private String stableId;
    @Builder.Default
    private int version = 1;
    private String content;
    private String name;
    // used to keep surveys attached to their portal even if they are not on an environment currently
    private UUID portalId;
}
