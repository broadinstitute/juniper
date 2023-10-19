package bio.terra.pearl.core.model.site;

import bio.terra.pearl.core.model.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/** Full persistent class that includes the actual serialized image */
@Getter @Setter @SuperBuilder @NoArgsConstructor
public class SiteImage extends SiteImageMetadata {
    private byte[] data;
}
