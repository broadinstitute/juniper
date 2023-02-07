package bio.terra.pearl.core.model.workflow;

import java.util.Arrays;

/**
 * This is not, and likely should not be, an exhaustive list of all possible task statuses.  Rather, it
 * is a collection of common task statuses that might be shared across several types of tasks.
 * for example, Kit Requests might have 15 different statuses.
 * we can make a decision when we get to that of whether that belongs in a separate field,
 * or whether TaskStatus will become an interface, etc...
 */
public enum TaskStatus {
    NEW,
    IN_PROGRESS,
    COMPLETE,
    REJECTED;

    public boolean isTerminalStatus() {
        return Arrays.asList(COMPLETE, REJECTED).contains(this);
    }
}
