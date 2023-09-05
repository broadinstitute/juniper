package bio.terra.pearl.core.model.survey;

import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.Versioned;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter @Setter @NoArgsConstructor @SuperBuilder
public class Survey extends BaseEntity implements Versioned {
    private String stableId;
    @Builder.Default
    private int version = 1;
    private Integer publishedVersion;
    private String content;
    private String name;
    // used to keep surveys attached to their portal even if they are not on an environment currently
    private UUID portalId;
    @Builder.Default
    private List<AnswerMapping> answerMappings = new ArrayList<>();
    // markdown to be displayed below every page of the survey
    private String footer;
}

