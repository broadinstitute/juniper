package bio.terra.pearl.core.service.workflow;

import bio.terra.pearl.core.model.workflow.TaskType;

import java.util.List;
import java.util.UUID;

public record ParticipantTaskAssignDto (
        TaskType taskType,
        String targetStableId,
        int targetAssignedVersion,
        List<UUID> enrolleeIds,
        // if true, the enrolleeIds list will be ignored and tasks will be assigned to all enrollees
        // not already having the task in the duplicate window
        boolean assignAllUnassigned,
        // if true, the tasks will be assigned regardless of any rules on the tasks -- this allows
        // study staff to override duplication or other criteria on the tasks themselves.
        // obviously, this should be used with caution
        boolean overrideEligibility
) {}


