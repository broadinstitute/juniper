package bio.terra.pearl.core.model.publishing;

import bio.terra.pearl.core.model.BaseEntity;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class PortalEnvironmentChangeRecord extends BaseEntity {
    private UUID adminUserId;
    // json blob stored as string for convenience
    private String portalEnvironmentChange;
}
