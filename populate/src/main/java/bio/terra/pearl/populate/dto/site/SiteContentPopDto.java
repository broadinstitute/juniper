package bio.terra.pearl.populate.dto.site;

import bio.terra.pearl.core.model.site.SiteContent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter @Setter @NoArgsConstructor
public class SiteContentPopDto extends SiteContent {
    private List<SiteImagePopDto> siteImageDtos = new ArrayList<>();
    private Set<LocalizedSiteContentPopDto> localizedSiteContentDtos = new HashSet<>();
}
