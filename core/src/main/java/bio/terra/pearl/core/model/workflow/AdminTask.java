package bio.terra.pearl.core.model.workflow;

import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantNote;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;


/**
 * represents a thing that an adminUser can do, or has already done.  e.g. a participant note might be assigned to
 * a staff member to follow-up
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class AdminTask extends BaseEntity {
    private Instant completedAt;
    @Builder.Default
    private TaskStatus status = TaskStatus.NEW;
    @Builder.Default
    private AdminTaskType taskType = AdminTaskType.GENERAL;
    private UUID studyEnvironmentId;
    private UUID enrolleeId;
    private UUID participantNoteId;
    private UUID creatingAdminUserId;
    private UUID assignedAdminUserId;
    private String description;
    private String dispositionNote;
}