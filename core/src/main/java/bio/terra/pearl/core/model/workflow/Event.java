package bio.terra.pearl.core.model.workflow;

import bio.terra.pearl.core.model.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

/**
 * Simple record of an event occurring, primarily for debugging and customer support.
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Event extends BaseEntity {
    private EventClass eventClass;
    private UUID studyEnvironmentId;
    private UUID portalEnvironmentId;
    private UUID enrolleeId;
}
