package bio.terra.pearl.core.service.workflow;

import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.notification.Trigger;
import bio.terra.pearl.core.model.notification.TriggerActionType;
import bio.terra.pearl.core.model.notification.TriggerScope;
import bio.terra.pearl.core.model.notification.TriggerType;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.service.admin.AdminUserService;
import bio.terra.pearl.core.service.notification.NotificationDispatcher;
import bio.terra.pearl.core.service.notification.TriggerService;
import bio.terra.pearl.core.service.notification.email.AdminEmailService;
import bio.terra.pearl.core.service.notification.email.EmailTemplateService;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.core.service.rule.EnrolleeRuleEvaluator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * listens for events, finds any correspond action triggers for the study environment,
 * and then executes the actions
 */
@Service
@Slf4j
public class TriggerActionService {
    private final TriggerService triggerService;
    private final NotificationDispatcher notificationDispatcher;
    private final ParticipantTaskService participantTaskService;
    private final AdminEmailService adminEmailService;
    private final PortalService portalService;
    private final EmailTemplateService emailTemplateService;
    private final AdminUserService adminUserService;

    public TriggerActionService(TriggerService triggerService, NotificationDispatcher notificationDispatcher, ParticipantTaskService participantTaskService, AdminEmailService adminEmailService, PortalService portalService, EmailTemplateService emailTemplateService, AdminUserService adminUserService) {
        this.triggerService = triggerService;
        this.notificationDispatcher = notificationDispatcher;
        this.participantTaskService = participantTaskService;
        this.adminEmailService = adminEmailService;
        this.portalService = portalService;
        this.emailTemplateService = emailTemplateService;
        this.adminUserService = adminUserService;
    }

    /** actions could be triggered by just about anything, so listen to all enrollee events */
    @EventListener
    @Order(DispatcherOrder.ACTION)
    public void handleEvent(EnrolleeEvent event) {
        List<Trigger> applicableTriggers = triggerService
                .findByStudyEnvironmentId(event.getEnrollee().getStudyEnvironmentId(), true)
                // only EVENT triggers
                .stream().filter(trigger  -> trigger.getTriggerType().equals(TriggerType.EVENT))
                // that match the event type
                .filter(trigger -> trigger.getEventType().eventClass.isInstance(event))
                // that satisfy the trigger's rule
                .filter(trigger -> EnrolleeRuleEvaluator.evaluateRule(trigger.getRule(), event.getEnrolleeContext()))
                .toList();

        for (Trigger trigger: applicableTriggers) {
            if (TriggerActionType.NOTIFICATION.equals(trigger.getActionType())) {
                notificationDispatcher.dispatchNotificationAsync(trigger, event.getEnrolleeContext(),
                        event.getPortalParticipantUser().getPortalEnvironmentId());
            } else if (TriggerActionType.ADMIN_NOTIFICATION.equals(trigger.getActionType())) {
                try {
                    adminEmailService.sendEmailFromTrigger(trigger, event);
                } catch (Exception e) {
                    log.error("Failed to send admin email for trigger {}", trigger.getId(), e);
                }
            } else if (TriggerActionType.TASK_STATUS_CHANGE.equals(trigger.getActionType())) {
                updateTaskStatus(trigger, event);
            }
        }
    }


    /**
     * for tasks of type TASK_STATUS_CHANGE, update the task status
     */
    protected void updateTaskStatus(Trigger trigger, EnrolleeEvent event) {
        List<ParticipantTask> tasks;
        // find the task(s) to update
        if (TriggerScope.STUDY.equals(trigger.getActionScope())) {
            tasks = participantTaskService.findTasksByStudyAndTarget(trigger.getStudyEnvironmentId(), List.of(trigger.getUpdateTaskTargetStableId()));
        } else {
            tasks = participantTaskService.findByPortalParticipantUserId(event.getPortalParticipantUser().getId());
        }
        tasks = tasks.stream().filter(task -> Objects.equals(task.getTargetStableId(), trigger.getUpdateTaskTargetStableId())).toList();
        tasks.stream().forEach(task -> {
            task.setStatus(trigger.getStatusToUpdateTo());
            participantTaskService.update(task, createAuditInfo(event,"updateTaskStatus"));
        });
    }

    protected DataAuditInfo createAuditInfo(EnrolleeEvent event, String methodName) {
        return DataAuditInfo.builder()
                .systemProcess(DataAuditInfo.systemProcessName(getClass(), methodName))
                .enrolleeId(event.getEnrollee().getId())
                .portalParticipantUserId(event.getPortalParticipantUser().getId())
                .build();
    }
}
