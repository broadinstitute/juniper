package bio.terra.pearl.populate.dto.participant;

import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.populate.dto.TimeShiftable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Note there was a choice here between "synthetic patients are populated by specifying exact task states"
 * vs. "synthetic patients are populated by specifying events that occurred."
 * For the case of newbie, one could imagine an alternate population strategy where the enrolleee.json file was
 * say something like `eventToFire: "EnrolleeCreationEvent"` and then the task list would be generated programatically
 * from that event.  While that would be more realistic in some ways, it would not be as flexible as the approach
 * of allowing exact task specification for representing various states of completion and change.
  */

@Getter
@Setter
@NoArgsConstructor
public class ParticipantTaskPopDto extends ParticipantTask implements TimeShiftable {
    // e.g. now-1d
    private String completedAtOffset;
    private Integer submittedHoursAgo;
    private String assignedToUsername;
    private String creatingAdminUsername;
}
