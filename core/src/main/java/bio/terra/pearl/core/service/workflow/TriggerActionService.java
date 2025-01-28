package bio.terra.pearl.core.service.workflow;

import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.notification.Trigger;
import bio.terra.pearl.core.model.notification.TriggerActionType;
import bio.terra.pearl.core.model.notification.TriggerScope;
import bio.terra.pearl.core.model.notification.TriggerType;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.search.EnrolleeSearchExpressionResult;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.service.admin.AdminUserService;
import bio.terra.pearl.core.service.notification.NotificationDispatcher;
import bio.terra.pearl.core.service.notification.TriggerService;
import bio.terra.pearl.core.service.notification.email.AdminEmailService;
import bio.terra.pearl.core.service.notification.email.EmailTemplateService;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.core.service.rule.EnrolleeContext;
import bio.terra.pearl.core.service.rule.EnrolleeContextService;
import bio.terra.pearl.core.service.rule.EnrolleeRuleEvaluator;
import bio.terra.pearl.core.service.search.EnrolleeSearchExpressionParser;
import bio.terra.pearl.core.service.search.EnrolleeSearchService;
import lombok.extern.slf4j.Slf4j;
import org.jooq.tools.StringUtils;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Listens for events, finds any correspond action triggers for the study environment,
 * and then executes the actions.
 *
 *  This service is the custom counterpart to TaskDispatcher and its subclasses.  While those classes handle
 *  hard-coded Juniper event consequences (e.g. when a participant enrolls, assign them all eligible surveys, or
 *  when a participant completes all their consent forms, mark them as consented), this service handles custom triggered actions
 *
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
    private final EnrolleeSearchService enrolleeSearchService;
    private final EnrolleeService enrolleeService;
    private final EnrolleeContextService enrolleeContextService;
    private final PortalParticipantUserService portalParticipantUserService;
    private final EnrolleeSearchExpressionParser enrolleeSearchExpressionParser;

    public TriggerActionService(TriggerService triggerService, NotificationDispatcher notificationDispatcher, ParticipantTaskService participantTaskService, AdminEmailService adminEmailService, PortalService portalService, EmailTemplateService emailTemplateService, AdminUserService adminUserService, EnrolleeSearchService enrolleeSearchService, EnrolleeService enrolleeService, EnrolleeContextService enrolleeContextService, PortalParticipantUserService portalParticipantUserService, EnrolleeSearchExpressionParser enrolleeSearchExpressionParser) {
        this.triggerService = triggerService;
        this.notificationDispatcher = notificationDispatcher;
        this.participantTaskService = participantTaskService;
        this.adminEmailService = adminEmailService;
        this.portalService = portalService;
        this.emailTemplateService = emailTemplateService;
        this.adminUserService = adminUserService;
        this.enrolleeSearchService = enrolleeSearchService;
        this.enrolleeService = enrolleeService;
        this.enrolleeContextService = enrolleeContextService;
        this.portalParticipantUserService = portalParticipantUserService;
        this.enrolleeSearchExpressionParser = enrolleeSearchExpressionParser;
    }

    /** actions could be triggered by just about anything, so listen to all enrollee events */
    @EventListener
    @Order(DispatcherOrder.ACTION)
    public void handleEnrolleeEvent(EnrolleeEvent event) {
        List<Trigger> applicableTriggers = triggerService
                .findByStudyEnvironmentId(event.getEnrollee().getStudyEnvironmentId(), true)
                // only EVENT triggers
                .stream().filter(trigger  -> trigger.getTriggerType().equals(TriggerType.EVENT))
                // that match the event type
                .filter(trigger -> trigger.getEventType().eventClass.isInstance(event))
                // that match the trigger's event target (if a target is specified)
                .filter(trigger -> trigger.getFilterTargetStableIds().isEmpty() || trigger.getFilterTargetStableIds().contains(event.getTargetStableId()))
                // that satisfy the trigger's rule
                .filter(trigger -> EnrolleeRuleEvaluator.evaluateRule(trigger.getRule(), event.getEnrolleeContext()))
                .toList();

        for (Trigger trigger: applicableTriggers) {
            // admin notifications can only happen on enrollee events
            if (TriggerActionType.ADMIN_NOTIFICATION.equals(trigger.getActionType())) {
                try {
                    adminEmailService.sendEmailFromTrigger(trigger, event);
                } catch (Exception e) {
                    log.error("Failed to send admin email for trigger {}", trigger.getId(), e);
                }
            } else {
                executeAction(event.getEnrolleeContext(), trigger, event.getPortalParticipantUser().getPortalEnvironmentId());
            }
        }
    }

    @EventListener
    @Order(DispatcherOrder.ACTION)
    public void handleStudyEvent(StudyEvent event) {
        List<Trigger> applicableTriggers = triggerService
                .findByStudyEnvironmentId(event.getStudyEnvironmentId(), true)
                // only EVENT triggers
                .stream().filter(trigger -> trigger.getTriggerType().equals(TriggerType.EVENT))
                // that match the event type
                .filter(trigger -> trigger.getEventType().eventClass.isInstance(event))
                // that match the trigger's event target (if a target is specified)
                .filter(trigger -> trigger.getFilterTargetStableIds().isEmpty() || trigger.getFilterTargetStableIds().contains(event.getTargetStableId()))
                .toList();

        for (Trigger trigger : applicableTriggers) {
            List<EnrolleeContext> enrolleeContexts;

            if (!StringUtils.isEmpty(trigger.getRule())) {
                enrolleeContexts = enrolleeSearchService.executeSearchExpression(
                                event.getStudyEnvironmentId(), trigger.getRule() + " and include({user.createdAt})")
                        .stream().map(EnrolleeSearchExpressionResult::toEnrolleeContext).toList();
            } else {
                enrolleeContexts = enrolleeContextService.fetchDataByStudyEnvId(event.getStudyEnvironmentId());
            }

            for (EnrolleeContext enrolleeContext : enrolleeContexts) {
                executeAction(enrolleeContext, trigger, event.getPortalEnvironmentId());
            }
        }
    }

    private void executeAction(EnrolleeContext enrolleeContext, Trigger trigger, UUID portalEnvironmentId) {
        if (TriggerActionType.NOTIFICATION.equals(trigger.getActionType())) {
            notificationDispatcher.dispatchNotificationAsync(trigger, enrolleeContext, portalEnvironmentId);
        } else if (TriggerActionType.TASK_STATUS_CHANGE.equals(trigger.getActionType())) {
            updateTaskStatus(trigger, enrolleeContext);
        }
    }

    /**
     * for tasks of type TASK_STATUS_CHANGE, update the task status
     */
    protected void updateTaskStatus(Trigger trigger, EnrolleeContext enrolleeContext) {
        PortalParticipantUser ppUser = portalParticipantUserService.findForEnrollee(enrolleeContext.getEnrollee());

        List<ParticipantTask> tasks;
        // find the task(s) to update
        if (TriggerScope.STUDY.equals(trigger.getActionScope())) {
            tasks = participantTaskService.findTasksByStudyAndTarget(trigger.getStudyEnvironmentId(), trigger.getActionTargetStableIds());
        } else {
            tasks = participantTaskService.findByPortalParticipantUserId(ppUser.getId());
        }
        tasks = tasks.stream().filter(task -> trigger.getActionTargetStableIds().contains(task.getTargetStableId())).toList();
        tasks.stream().forEach(task -> {
            task.setStatus(trigger.getStatusToUpdateTo());
            participantTaskService.update(task, createAuditInfo(enrolleeContext.getEnrollee(), ppUser, "updateTaskStatus"));
        });
    }

    protected DataAuditInfo createAuditInfo(Enrollee enrollee, PortalParticipantUser ppUser, String methodName) {
        return DataAuditInfo.builder()
                .systemProcess(DataAuditInfo.systemProcessName(getClass(), methodName))
                .enrolleeId(enrollee.getId())
                .portalParticipantUserId(ppUser.getId())
                .build();
    }
}
