package bio.terra.pearl.core.model.notification;

import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.Versioned;
import bio.terra.pearl.core.model.publishing.VersionedEntityConfig;
import bio.terra.pearl.core.model.workflow.TaskType;
import java.time.Duration;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * configuration for notifications.
 * This probably should have subclasses for each NotificationType with single-table inheritance,
 * but BaseJDBIDao doesn't support that yet, so to keep our DAO infrastructure simple, we represent it as a single class.
 *
 * NotificationConfigs should be treated as generally immutable so that logs of notifications can point to their
 * originating configuration. To make changes, the previous config should be deactivated, and a new one created
 */
@Getter @Setter @SuperBuilder @NoArgsConstructor
public class NotificationConfig extends BaseEntity implements VersionedEntityConfig {
    private UUID studyEnvironmentId;
    private UUID portalEnvironmentId;
    @Builder.Default
    private boolean active = true;

    private NotificationType notificationType;
    @Builder.Default
    private NotificationDeliveryType deliveryType = NotificationDeliveryType.EMAIL;
    private UUID emailTemplateId;
    private EmailTemplate emailTemplate;
    private String rule;

    private NotificationEventType eventType; // for notificationTypes of ON_EVENT
    /**
     * notificationTypes of ON_TASK, if specified, will limit to one type of task.  if null,
     * will apply to all tasks.
     */
    private TaskType taskType;
    private String taskTargetStableId; // for notificationTypes of ON_TASK
    /**
     * for notificationTypes of ON_TASK -- if specified
        this will delay sending until the specified number of minutes have passed while the task is in an incomplete state
     */
    @Builder.Default
    private int afterMinutesIncomplete = (int) Duration.ofHours(72).toMinutes();
    @Builder.Default
    private int reminderIntervalMinutes = (int) Duration.ofHours(72).toMinutes();
    @Builder.Default
    private int maxNumReminders = -1; // -1 means to keep reminding indefinitely

    @Override
    public Versioned getVersionedEntity() {
        return emailTemplate;
    }
}
