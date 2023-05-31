package bio.terra.pearl.core.model.notification;

public enum NotificationType {
    EVENT, // notification will happen on a given event
    TASK_REMINDER, // notification will happen after a task is incomplete for a set period of time
    AD_HOC // one-off email sent manually by study staff
}
