package bio.terra.pearl.api.participant.models.mixins;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Prevents serializing research IDs in the participant API as they could be used in research and be
 * re-identifiable.
 */
public abstract class DoNotSerializeResearchIdMixin {

  @JsonIgnore
  public abstract String getResearchId();
}
