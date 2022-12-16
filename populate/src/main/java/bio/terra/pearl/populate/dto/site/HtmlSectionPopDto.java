package bio.terra.pearl.populate.dto.site;

import bio.terra.pearl.core.model.site.HtmlSection;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class HtmlSectionPopDto extends HtmlSection {
    private JsonNode sectionConfigJson;
}
