package bio.terra.pearl.core.model.portal;

import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.EnvironmentName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class PortalEnvironment extends BaseEntity {
    private EnvironmentName environmentName;
    private UUID portalId;
    private UUID portalEnvironmentConfigId;
    private PortalEnvironmentConfig portalEnvironmentConfig;
}
