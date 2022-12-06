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

@Getter
@Setter
@SuperBuilder @NoArgsConstructor
public class SurveyResponse extends BaseEntity {
    private UUID enrolleeId;
    private UUID participantUserId;
    private UUID adminUserId;
    private UUID surveyId;
    private UUID lastSnapshotId;
    @Builder.Default
    private boolean complete = false;
    private ResponseSnapshot lastSnapshot;
    @Builder.Default
    private Set<ResponseSnapshot> snapshots = new HashSet<>();
}
