package bio.terra.pearl.populate.dto.site;

import bio.terra.pearl.core.model.site.HtmlPage;
import bio.terra.pearl.core.model.site.NavbarItem;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter @NoArgsConstructor
public class NavbarItemPopDto extends NavbarItem {
    private String populateFileName;

    // kept here for legacy reasons
    private HtmlPage htmlPage;
    private HtmlPagePopDto htmlPageDto;

    // for grouped navbars
    private List<NavbarItemPopDto> itemDtos = new ArrayList<>();
}
