package bio.terra.pearl.core.service.workflow;


import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * contains data associated with enrollee creation.
 * To avoid requerying data over and over again, the enrollee in the event should be updated in-place as the event
 * propagates.  For example, if a listener creates a task for the enrollee, that task should be added to the Enrollee
 * object attached to the event.  That way later consumers will be able to see the tasks without reloading the DB
 * every time.
 */
@Getter @Setter @SuperBuilder
public class EnrolleeCreationEvent extends EnrolleeEvent {

}
