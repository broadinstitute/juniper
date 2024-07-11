package bio.terra.pearl.core.model.workflow;

public enum TaskType {
    CONSENT,
    SURVEY, // a research survey
    OUTREACH, // an outreach activity -- not essential for research
    KIT_REQUEST,
    ADMIN_FORM, // a task for study staff to complete -- not visible to participants
    ADMIN_NOTE // a form for study staff to complete -- not visible to participants
}
