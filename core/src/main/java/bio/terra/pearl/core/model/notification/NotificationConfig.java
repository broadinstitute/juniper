package bio.terra.pearl.core.model.notification;

import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.workflow.TaskType;
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
 */
@Getter @Setter @SuperBuilder @NoArgsConstructor
public class NotificationConfig extends BaseEntity {
    private UUID studyEnvironmentId;
    private UUID portalEnvironmentId;

    private NotificationType notificationType;
    @Builder.Default
    private NotificationDeliveryType deliveryType = NotificationDeliveryType.EMAIL;

    private String rule;

    private NotificationEventType eventType; // for notificationTypes of ON_EVENT
    /**
     * notificationTypes of ON_TASK, if specified, will limit to one type of task.  if null,
     * will apply to all tasks of that type.
     */
    private TaskType taskType;
    private String taskTargetStableId; // for notificationTypes of ON_TASK
    /**
     * for notificationTypes of ON_TASK -- if specified
        this will delay sending until the specified number of minutes have passed while the task is in an incomplete state
     */
    private int afterMinutesIncomplete;
}
