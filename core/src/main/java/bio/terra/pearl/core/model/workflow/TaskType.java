package bio.terra.pearl.core.model.workflow;

public enum TaskType {
    CONSENT,
    SURVEY, // a research survey
    OUTREACH, // an outreach activity -- not essential for research
    KIT_REQUEST,
    ADMIN // a task for study staff to complete -- not visible to participants
}
