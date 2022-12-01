package bio.terra.pearl.populate.dto;

import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter @Setter @NoArgsConstructor
public class PortalPopDto extends Portal {
    private Set<String> populateStudyFiles = new HashSet<>();
    private List<PortalEnvironmentPopDto> portalEnvironmentDtos;
    /**
     * this allows us to pass a PopulatePortalDto to PortalService.create and have the specified environments
     * in portalEnvironmentDtos used for the creation.
     */
    public Set<PortalEnvironment> getPortalEnvironments() {
        return portalEnvironmentDtos.stream().collect(Collectors.toSet());
    }
}
