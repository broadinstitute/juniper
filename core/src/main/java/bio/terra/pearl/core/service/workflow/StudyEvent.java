package bio.terra.pearl.core.service.workflow;

import bio.terra.pearl.core.model.workflow.Event;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class StudyEvent extends Event implements BaseEvent{

    /**
     * the targetStableId of a task that generated this event (e.g. the stableId of the survey that was completed or the kitType name)
     * will be null for events that are not task-specific (e.g. enrollment)
     * */
    public String getTargetStableId() {
        return null;
    }
}
