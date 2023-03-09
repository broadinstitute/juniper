package bio.terra.pearl.core.model.notification;

public enum NotificationDeliveryStatus {
    READY,
    SENT,
    SKIPPED, // skipped due to a user preference or other configuration issue (e.g. missing address)
    FAILED
}
