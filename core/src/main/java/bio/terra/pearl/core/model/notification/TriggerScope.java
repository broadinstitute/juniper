package bio.terra.pearl.core.model.notification;

public enum TriggerScope {
    PORTAL, // this trigger should apply to any relevant tasks in the portal
    STUDY // this trigger should apply to any relevant tasks in the study of the event that triggered it
}
