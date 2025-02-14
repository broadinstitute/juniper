package bio.terra.pearl.core.model.notification;

import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.Versioned;
import bio.terra.pearl.core.model.publishing.VersionedEntityConfig;
import bio.terra.pearl.core.model.study.StudyEnvAttached;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import bio.terra.pearl.core.model.workflow.TaskType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * configuration for notifications.
 * This probably should have subclasses for each NotificationType with single-table inheritance,
 * but BaseJDBIDao doesn't support that yet, so to keep our DAO infrastructure simple, we represent it as a single class.
 *
 * NotificationConfigs should be treated as generally immutable so that logs of notifications can point to their
 * originating configuration. To make changes, the previous config should be deactivated, and a new one created
 */
@Getter @Setter @SuperBuilder @NoArgsConstructor
public class Trigger extends BaseEntity implements VersionedEntityConfig, StudyEnvAttached {
    private UUID studyEnvironmentId;
    private UUID portalEnvironmentId;
    @Builder.Default
    private boolean active = true;

    private TriggerType triggerType;
    private TriggerEventType eventType; // for notificationTypes of EVENT
    @Builder.Default
    private TriggerActionType actionType = TriggerActionType.NOTIFICATION;
    @Builder.Default // narrows the scope of the trigger to tasks with these stableIds
    private List<String> filterTargetStableIds = new ArrayList<>();

    // fields below are for triggerActionType of TASK_STATUS_CHANGE
    @Builder.Default
    private TriggerScope actionScope = TriggerScope.STUDY;
    private TaskStatus statusToUpdateTo;

    @Builder.Default // the trigger will create an action related to these task stableIds
    private List<String> actionTargetStableIds = new ArrayList<>();

    // fields below are for triggerActionType of NOTIFICATION
    @Builder.Default
    private NotificationDeliveryType deliveryType = NotificationDeliveryType.EMAIL;
    private UUID emailTemplateId;
    private EmailTemplate emailTemplate;
    private String rule;

    // for admin notifications, comma separated list of admin emails.
    // will not send if the email does not have an associated admin account
    // if blank, no admins will be notified
    private String adminEmailFilter;
    /**
     * notificationTypes of TASK_REMINDER, if specified, will limit to one type of task.  if null,
     * will apply to all tasks.
     */
    private TaskType taskType;
    /**
     * for notificationTypes of TASK_REMINDER -- if specified
        this will delay sending until the specified number of minutes have passed while the task is in an incomplete state
     */
    @Builder.Default
    private int afterMinutesIncomplete = (int) Duration.ofHours(72).toMinutes();
    @Builder.Default
    private int reminderIntervalMinutes = (int) Duration.ofHours(72).toMinutes();
    @Builder.Default
    private int maxNumReminders = 5; // -1 means to keep reminding indefinitely
    @Override
    public Versioned versionedEntity() {
        return emailTemplate;
    }
    @Override
    public UUID versionedEntityId() { return emailTemplateId; }
    @Override
    public void updateVersionedEntityId(UUID emailTemplateId) {
        setEmailTemplateId(emailTemplateId);
    }
}
