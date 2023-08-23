package bio.terra.pearl.core.model.site;

import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.Versioned;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.*;

/** Top level construct for representing a home page of a portal or study.
 * Contains landing pages and associated links and content, in multiple translations */
@Getter @Setter @NoArgsConstructor @SuperBuilder
public class SiteContent extends BaseEntity implements Versioned {
    private String stableId;
    private int version;
    @Builder.Default
    private List<LocalizedSiteContent> localizedSiteContents = new ArrayList<>();
    @Builder.Default
    private String defaultLanguage = "en";

    // used to keep siteContents attached to a portal even if they are not on an environment currently
    private UUID portalId;
}
