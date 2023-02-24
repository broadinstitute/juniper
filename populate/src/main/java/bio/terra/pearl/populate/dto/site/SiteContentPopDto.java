package bio.terra.pearl.populate.dto.site;

import bio.terra.pearl.core.model.site.SiteContent;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class SiteContentPopDto extends SiteContent {
    private Set<LocalizedSiteContentPopDto> localizedSiteContentDtos = new HashSet<>();
}
