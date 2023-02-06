package bio.terra.pearl.populate.dto;

import bio.terra.pearl.core.model.workflow.ParticipantTask;

/**
 * Note there was a choice here between "synthetic patients are populated by specifying exact task states"
 * vs. "synthetic patients are populated by specifying events that occurred."
 * For the case of newbie, one could imagine an alternate population strategy where the enrolleee.json file was
 * say something like `eventToFire: "EnrolleeCreationEvent"` and then the task list would be generated programatically
 * from that event.  While that would be more realistic in some ways, it would not be as flexible as the approach
 * of allowing exact task specification for representing various states of completion and change.
  */

public class ParticipantTaskPopDto extends ParticipantTask {
    // e.g. now-1d
    private String completedAtOffset;
}
