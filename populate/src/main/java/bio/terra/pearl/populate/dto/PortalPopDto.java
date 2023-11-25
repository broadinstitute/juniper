package bio.terra.pearl.populate.dto;

import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.populate.dto.site.SiteImagePopDto;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class PortalPopDto extends Portal {
    private List<String> surveyFiles = new ArrayList<>(); // surveys that are not specific to a study (e.g. preReg)
    private List<String> populateStudyFiles = new ArrayList<>();
    private List<String> siteContentFiles = new ArrayList<>();
    private List<PortalEnvironmentPopDto> portalEnvironmentDtos = new ArrayList<>();
    private List<SiteImagePopDto> siteImageDtos = new ArrayList<>();
    /**
     * this allows us to pass a PopulatePortalDto to PortalService.create and have the specified environments
     * in portalEnvironmentDtos used for the creation.
     */
    public Set<PortalEnvironment> getPortalEnvironments() {
        return portalEnvironmentDtos.stream().collect(Collectors.toSet());
    }
    public List<AdminUserDto> adminUsers = new ArrayList<>();
}
