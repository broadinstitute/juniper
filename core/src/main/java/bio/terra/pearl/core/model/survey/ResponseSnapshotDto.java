package bio.terra.pearl.core.model.survey;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * convenience dto for snapshot received from the frontend.  Allows a completed property to indicate
 * whether this submission should complete the associated response.
 */
@Getter  @NoArgsConstructor
@Setter @SuperBuilder
public class ResponseSnapshotDto extends ParsedSnapshot {
    private boolean complete;
}
