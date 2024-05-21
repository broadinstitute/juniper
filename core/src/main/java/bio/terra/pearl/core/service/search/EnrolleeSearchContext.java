package bio.terra.pearl.core.service.search;

import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.Profile;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Context for evaluating a search expression on an enrollee.
 */
@Getter
@SuperBuilder
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EnrolleeSearchContext {
    private Enrollee enrollee;
    private Profile profile;
}
