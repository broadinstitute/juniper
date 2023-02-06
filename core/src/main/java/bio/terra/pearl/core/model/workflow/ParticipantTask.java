package bio.terra.pearl.core.model.workflow;

import bio.terra.pearl.core.model.BaseEntity;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * represents a thing that a participant can do, or has already done.  Tasks should probably not be created until they
 * are confirmed to be applicable to the participant.  I'm still waffling on whether it makes
 * sense to create tasks when the action is blocked by another action.  For example, do we create tasks for
 * surveys to complete, even before the participant has consented?  Or do we only create those tasks once the
 * participant has consented.  I lean towards the latter, because I'd rather not have to clean up or manage
 * stale/inapplicable tasks, (e.g. don't create a "heart attack follow-up survey" task for a participant until
 * they've completed a prior survey that confirms they've had a heart attack.
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class ParticipantTask extends BaseEntity {
    private Instant completedAt;
    private TaskStatus status;
    private TaskType taskType;
    private String targetName;
    private String targetStableId;
    private int targetAssignedVersion;
    private int taskOrder;
    private boolean blocksHub;
    private UUID studyEnvironmentId;
    private UUID enrolleeId;
    private UUID portalParticipantUserId;
}


