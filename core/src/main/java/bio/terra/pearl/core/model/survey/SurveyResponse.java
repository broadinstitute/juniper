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
    // the latest survey data JSON, stored as a string for performance/convenience
    // this is the result of running merge on all the snapshots in order
    private String latestData;
    @Builder.Default
    private boolean complete = false;
    @Builder.Default
    private Set<ResponseSnapshot> snapshots = new HashSet<>();
}
