package bio.terra.pearl.core.service.consent;

import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import bio.terra.pearl.core.model.workflow.TaskType;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.search.EnrolleeSearchExpressionParser;
import bio.terra.pearl.core.service.workflow.DispatcherOrder;
import bio.terra.pearl.core.service.workflow.EnrolleeEvent;
import bio.terra.pearl.core.service.workflow.ParticipantTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.util.List;

/** Holds logic for building and processing consent tasks, and event listeners for triggering updates */
@Service
@Slf4j
public class ConsentTaskDispatcher {
    private ParticipantTaskService participantTaskService;
    private EnrolleeService enrolleeService;
    private EnrolleeSearchExpressionParser enrolleeSearchExpressionParser;

    public ConsentTaskDispatcher(ParticipantTaskService participantTaskService,
                                 EnrolleeService enrolleeService,
                                 EnrolleeSearchExpressionParser enrolleeSearchExpressionParser) {
        this.participantTaskService = participantTaskService;
        this.enrolleeService = enrolleeService;
        this.enrolleeSearchExpressionParser = enrolleeSearchExpressionParser;
    }

    /**
     * We want to listen to enrollee creation events and consent response events.  (but not survey events)
     * see https://stackoverflow.com/questions/45884537/use-eventlistener-annotation-on-multiple-events-in-spring
     * for why we use two separate listening methods to accomplish that
     */
    @EventListener
    @Order(DispatcherOrder.CONSENT_TASK)
    public void handleEvent(EnrolleeConsentEvent enrolleeEvent) {
        updateConsentTasks(enrolleeEvent);
    }

    public void updateConsentTasks(EnrolleeEvent enrolleeEvent) {
        updateEnrolleeConsented(enrolleeEvent.getEnrollee(), enrolleeEvent.getEnrollee().getParticipantTasks());
    }

    /** check the enrollee's current consent status, and update it if needed.  this will handle both
     * updating the DB and the enrollee object in-place */
    public Enrollee updateEnrolleeConsented(Enrollee enrollee, List<ParticipantTask> participantTasks) {
        boolean consentStatus = checkIsEnrolleeConsented(participantTasks);
        if (enrollee.isConsented() != consentStatus) {
            enrollee.setConsented(consentStatus);
            enrolleeService.updateConsented(enrollee.getId(), consentStatus);
        }
        return enrollee;
    }

    /** an enrollee is consented iff they have at least one completed consent and no outstanding consents */
    public static boolean checkIsEnrolleeConsented(List<ParticipantTask> participantTasks) {
        List<ParticipantTask> outstandingConsents = participantTasks.stream().filter(
                task -> task.getTaskType().equals(TaskType.CONSENT) && !task.getStatus().equals(TaskStatus.COMPLETE)
        ).toList();
        List<ParticipantTask> completedConsents = participantTasks.stream().filter(
                task -> task.getTaskType().equals(TaskType.CONSENT) && task.getStatus().equals(TaskStatus.COMPLETE)
        ).toList();
        return outstandingConsents.isEmpty() && !completedConsents.isEmpty();
    }
}
