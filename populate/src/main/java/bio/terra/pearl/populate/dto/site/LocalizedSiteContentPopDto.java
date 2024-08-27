package bio.terra.pearl.populate.dto.site;

import bio.terra.pearl.core.model.site.LocalizedSiteContent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
public class LocalizedSiteContentPopDto extends LocalizedSiteContent {
    private String landingPageFileName;

    private List<HtmlPagePopDto> pageDtos = new ArrayList<>();
    private List<NavbarItemPopDto> navbarItemDtos = new ArrayList<>();
    private String footerSectionFile;
}
