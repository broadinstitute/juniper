package bio.terra.pearl.core.model.site;

import bio.terra.pearl.core.model.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Class for storing metadata about a portal image.  Does not contain the serialized image data
 * SiteMedias are uniquely identified by the composite key of (cleanFileName, version, portalShortcode)
 * */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class SiteMediaMetadata extends BaseEntity {
    /**
     * the cleanFileName essentially serves as a stableId -- we call it cleanFileName here to indicate its origin.
     * and because it is not required to be globally unique like other stableIds -- it just has to be unique within a portal
     */
    private String cleanFileName;
    private int version;
    private String uploadFileName;
    // store these by portal shortcode to prioritize fast fetching based on urls that have the shortcode
    private String portalShortcode;
}
