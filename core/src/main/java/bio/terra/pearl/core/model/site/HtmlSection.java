package bio.terra.pearl.core.model.site;

import bio.terra.pearl.core.model.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter @Setter @SuperBuilder @NoArgsConstructor
public class HtmlSection extends BaseEntity {
    private HtmlSectionType sectionType;
    private String rawContent;
    private String sectionConfig;
    private int sectionOrder;
    private UUID htmlPageId;
}
