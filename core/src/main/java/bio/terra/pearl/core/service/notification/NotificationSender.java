package bio.terra.pearl.core.service.notification;

public interface NotificationSender {
    void sendNotificationAsync();
    void sendNotification();
}
