package bio.terra.pearl.core.model.portal;

import bio.terra.pearl.core.model.EnvironmentName;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class PortalEnvironmentId {
    private String shortcode;
    private EnvironmentName environmentName;
}
