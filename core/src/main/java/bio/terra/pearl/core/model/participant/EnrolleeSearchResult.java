package bio.terra.pearl.core.model.participant;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/** holder class to include an enrolle and their profile */
@Getter @Setter @Builder
public class EnrolleeSearchResult {
    private Enrollee enrollee;
    private Profile profile;
    private boolean hasKit;
}
