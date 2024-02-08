package bio.terra.pearl.api.admin.service.enrollee;

import bio.terra.pearl.core.model.workflow.TaskStatus;
import java.util.List;
import java.util.UUID;

public record ParticipantTaskUpdateDto(
    List<TaskUpdateSpec> updates,
    List<UUID> portalParticipantUserIds,
    boolean
        updateAll // if true, the portalParticipantUserIds list will be ignored and all tasks will
    // be updated in the environment
    ) {

  public record TaskUpdateSpec(
      String targetStableId,
      int updateToVersion,
      Integer updateFromVersion,
      TaskStatus newStatus) {}
}
