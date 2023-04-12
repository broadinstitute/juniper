package bio.terra.pearl.populate.dto.site;

import bio.terra.pearl.core.model.site.SiteImage;
import bio.terra.pearl.populate.dto.FilePopulatable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class SiteImagePopDto extends SiteImage implements FilePopulatable {
    private String populateFileName;
}
