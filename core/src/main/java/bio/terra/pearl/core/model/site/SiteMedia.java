package bio.terra.pearl.core.model.site;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Full persistent class that includes the actual serialized media
 */
@Getter @Setter @SuperBuilder @NoArgsConstructor
public class SiteMedia extends SiteMediaMetadata {
    private byte[] data;
}
