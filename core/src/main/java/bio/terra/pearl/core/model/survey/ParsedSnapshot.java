package bio.terra.pearl.core.model.survey;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Object model for survey response data that has been parsed.
 */
@Getter @Setter @NoArgsConstructor
@SuperBuilder
public class ParsedSnapshot extends ResponseSnapshot {
    private ResponseData parsedData;
}
