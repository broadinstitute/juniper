package bio.terra.pearl.core.model.site;

import bio.terra.pearl.core.model.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter @Setter @SuperBuilder @NoArgsConstructor
public class NavbarItem extends BaseEntity {
    private UUID localizedSiteContentId;
    private NavbarItemType itemType;
    private String text;
    private int itemOrder;

    private UUID htmlPageId; // for NavbarItemType INTERNAL
    private HtmlPage htmlPage; // for NavBarItemType INTERNAL

    private String href; // for NavBarItemType EXTERNAL and INTERNAL_ANCHOR
}
