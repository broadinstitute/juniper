package bio.terra.pearl.core.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter @Setter @SuperBuilder
public class PortalStudy extends BaseEntity {
    private UUID portalId;
    private UUID studyId;
}
