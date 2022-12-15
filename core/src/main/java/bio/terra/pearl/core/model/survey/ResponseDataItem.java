package bio.terra.pearl.core.model.survey;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Represents an answer to a single question */
@Getter @Setter @NoArgsConstructor
public class ResponseDataItem {
    private String stableId;
    private String simpleValue;
    private JsonNode value;
    private String displayValue;
    private boolean isNode = false;
}
