package bio.terra.pearl.core.model.participant;

import bio.terra.pearl.core.model.BaseEntity;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * represents a note about a participant. while for now it is only tied to an enrollee, as we add family/proxy support,
 * we'll add additional columns to e.g. link a note to both a parent and the child.
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class ParticipantNote extends BaseEntity {
  private String text;
  private UUID creatingAdminUserId;
  private UUID enrolleeId;
  private UUID kitRequestId;
}
