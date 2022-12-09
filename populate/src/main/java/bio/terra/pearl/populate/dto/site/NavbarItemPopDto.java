package bio.terra.pearl.populate.dto.site;

import bio.terra.pearl.core.model.site.NavbarItem;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class NavbarItemPopDto extends NavbarItem {
    private HtmlPagePopDto htmlPageDto;
}
