package bio.terra.pearl.core.service.workflow;

import bio.terra.pearl.core.model.workflow.RecurOn;
import bio.terra.pearl.core.model.workflow.RecurrenceType;

import java.util.UUID;

public interface TaskConfig {
    String getStableId();

    Integer getVersion();

    String getName();

    UUID getStudyEnvironmentId();

    RecurrenceType getRecurrenceType();

    RecurOn getRecurOn();

    Integer getDaysAfterEligible();

    Integer getRecurrenceIntervalDays();

    String getEligibilityRule();

    Boolean isAutoAssign();

    Boolean isAutoUpdateTaskAssignments();

    Boolean isAssignToExistingEnrollees();

    Boolean isRequired();

    Integer getTaskOrder();
}
