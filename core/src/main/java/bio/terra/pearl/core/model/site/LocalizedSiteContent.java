package bio.terra.pearl.core.model.site;

import bio.terra.pearl.core.model.BaseEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Holds content and navlinks for a specific translation of a site
 */
@Getter @Setter @SuperBuilder @NoArgsConstructor
public class LocalizedSiteContent extends BaseEntity {
    private UUID siteContentId;
    // language shortcode, e.g. "en" or "es"
    private String language;
    private UUID landingPageId;
    private HtmlPage landingPage;
    @Builder.Default
    private List<NavbarItem> navbarItems = new ArrayList<>();
    @Builder.Default
    private List<HtmlPage> pages = new ArrayList<>();
    private String navLogoCleanFileName;
    private int navLogoVersion;
    private UUID footerSectionId;
    private HtmlSection footerSection;
    private String primaryBrandColor;
    private String dashboardBackgroundColor;
}
