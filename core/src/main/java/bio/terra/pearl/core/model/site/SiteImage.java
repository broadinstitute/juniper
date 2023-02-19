package bio.terra.pearl.core.model.site;

import bio.terra.pearl.core.model.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter @Setter @SuperBuilder @NoArgsConstructor
public class SiteImage extends BaseEntity {
    private String cleanFileName;
    private int version;
    private String uploadFileName;
    private byte[] data;
    // store these by portal shortcode to prioritize fast fetching based on urls that have the shortcode
    private String portalShortcode;
}
