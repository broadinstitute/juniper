package bio.terra.pearl.core.service.participant.merge;

import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/** a record of an enrollee merge operation */
@Getter
@Setter
@Builder
public class ParticipantUserMerge {
    private MergeAction<ParticipantUser, ?> users;
    private MergeAction<PortalParticipantUser, ?> ppUsers;
    @Builder.Default
    private List<MergeAction<Enrollee, EnrolleeMerge>> enrollees = new ArrayList<>();
}


