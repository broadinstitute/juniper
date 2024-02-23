package bio.terra.pearl.populate.dto.site;

import bio.terra.pearl.core.model.site.SiteMedia;
import bio.terra.pearl.populate.dto.FilePopulatable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class SiteMediaPopDto extends SiteMedia implements FilePopulatable {
    private String populateFileName;
}
