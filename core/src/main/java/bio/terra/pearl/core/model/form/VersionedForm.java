package bio.terra.pearl.core.model.form;

import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.PortalAttached;
import bio.terra.pearl.core.model.Versioned;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public abstract class VersionedForm extends BaseEntity implements Versioned, PortalAttached {
    private String stableId;
    @Builder.Default
    private int version = 1;
    private Integer publishedVersion;
    private String content;
    private String name;

    // used to keep forms attached to their portal even if they are not on an environment currently
    private UUID portalId;
}
