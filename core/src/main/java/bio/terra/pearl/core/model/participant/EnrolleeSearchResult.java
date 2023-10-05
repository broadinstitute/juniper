package bio.terra.pearl.core.model.participant;

import bio.terra.pearl.core.model.kit.KitRequestStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/** holder class to include an enrolle and their profile */
@Getter @Setter @SuperBuilder
public class EnrolleeSearchResult {
    public EnrolleeSearchResult() {}
    private Enrollee enrollee;
    private ParticipantUser participantUser;
    private Profile profile;
    private KitRequestStatus mostRecentKitStatus;
}
