package bio.terra.pearl.core.model.notification;

public enum NotificationType {
    EVENT, // notification will happen on a given event
    TASK // notification will happen when (or after) a certain task is assigned to a participant
}
