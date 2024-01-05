package bio.terra.pearl.core.model.workflow;

import bio.terra.pearl.core.model.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

/**
 * Simple record of an application event occurring, used primarily for debugging and customer support.
 * Note that these records of application events (e.g., participant signup, kit received, etc..) are separate
 * from LogEvent's records, which log warnings and errors, and are essentially a store of the console log in the
 * database.
 * It might be the case that a single incident could trigger both an "Event" and a "LogEvent."
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
