package bio.terra.pearl.core.model.publishing;

import bio.terra.pearl.core.model.BaseEntity;
import java.util.UUID;

import bio.terra.pearl.core.model.EnvironmentName;
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
    private UUID portalId;
    private EnvironmentName environmentName;
    // json blob stored as string for convenience
    private String portalEnvironmentChange;
}
