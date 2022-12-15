package bio.terra.pearl.core.model.survey;

import bio.terra.pearl.core.model.BaseEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * container for response data from a given survey instance.
 */
@Getter
@Setter
@SuperBuilder @NoArgsConstructor
public class SurveyResponse extends BaseEntity {
    private UUID enrolleeId;
    private UUID creatingParticipantUserId;
    private UUID creatingAdminUserId;
    private UUID surveyId;
    private UUID lastSnapshotId;
    @Builder.Default
    private boolean complete = false;
    private ResponseSnapshot lastSnapshot;
    @Builder.Default
    private Set<ResponseSnapshot> snapshots = new HashSet<>();
}
