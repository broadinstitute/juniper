package bio.terra.pearl.populate.dto.site;

import bio.terra.pearl.core.model.site.HtmlPage;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter @NoArgsConstructor
public class HtmlPagePopDto extends HtmlPage {
    private List<HtmlSectionPopDto> sectionDtos = new ArrayList<>();
}
