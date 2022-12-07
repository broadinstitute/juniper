package bio.terra.pearl.populate.dto.site;

import bio.terra.pearl.core.model.site.SiteContent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter @NoArgsConstructor
public class SiteContentPopDto extends SiteContent {
    private List<SiteImagePopDto> siteImageDtos = new ArrayList<>();
}
