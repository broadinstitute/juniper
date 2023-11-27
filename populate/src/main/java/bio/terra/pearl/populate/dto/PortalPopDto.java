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
    /**
     * both surveys and consents can be populated at the portal level to both make sharing across studies easier
     *  and to allow for the possibility of a portal-level survey or consent
     */
    private List<String> surveyFiles = new ArrayList<>();
    private List<String> consentFormFiles = new ArrayList<>();
    private List<String> populateStudyFiles = new ArrayList<>();
    private List<String> siteContentFiles = new ArrayList<>();
    private List<PortalEnvironmentPopDto> portalEnvironmentDtos = new ArrayList<>();
    private List<SiteImagePopDto> siteImageDtos = new ArrayList<>();
    /**
     * this allows us to pass a PopulatePortalDto to PortalService.create and have the specified environments
     * in portalEnvironmentDtos used for the creation.
     */
    public List<PortalEnvironment> getPortalEnvironments() {
        return portalEnvironmentDtos.stream().collect(Collectors.toList());
    }
    public List<AdminUserDto> adminUsers = new ArrayList<>();
}
