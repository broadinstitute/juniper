package bio.terra.pearl.core.model.survey;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/** Represents an answer to a single question */
@Getter @Setter @SuperBuilder
@NoArgsConstructor
public class ResponseDataItem {
    private String stableId;
    private String simpleValue;
    private JsonNode value;
    private String displayValue;
    @Builder.Default
    private boolean isNode = false;
}
