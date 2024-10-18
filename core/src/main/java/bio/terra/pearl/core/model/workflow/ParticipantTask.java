package bio.terra.pearl.core.model.workflow;

import bio.terra.pearl.core.model.BaseEntity;
import java.time.Instant;
import java.util.UUID;

import bio.terra.pearl.core.model.study.StudyEnvAttached;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * represents a thing that a participant can do, or has already done.
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class ParticipantTask extends BaseEntity implements StudyEnvAttached {
    private Instant completedAt;
    private TaskStatus status;
    private TaskType taskType;
    private String targetName;
    private String targetStableId;
    private int targetAssignedVersion;
    private int taskOrder;
    private boolean blocksHub; // whether this task blocks the participant from doing optional tasks in the hub
    private UUID studyEnvironmentId;
    private UUID enrolleeId;
    private UUID assignedAdminUserId;
    private UUID portalParticipantUserId;
    private UUID surveyResponseId;
    private UUID kitRequestId;
    private UUID participantNoteId;
}


