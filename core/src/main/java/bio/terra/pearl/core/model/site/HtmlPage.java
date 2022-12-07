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

@Getter @Setter @SuperBuilder @NoArgsConstructor
public class HtmlPage extends BaseEntity {
    private String title; // the HTML <title> attribute content
    @Builder.Default
    private List<HtmlSection> sections = new ArrayList<>();
    private String path;
    private UUID localizedSiteContentId;
}
