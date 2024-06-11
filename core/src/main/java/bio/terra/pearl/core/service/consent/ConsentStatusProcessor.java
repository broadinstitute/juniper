package bio.terra.pearl.core.service.consent;

import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import bio.terra.pearl.core.model.workflow.TaskType;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.survey.event.EnrolleeSurveyEvent;
import bio.terra.pearl.core.service.workflow.DispatcherOrder;
import bio.terra.pearl.core.service.workflow.EnrolleeEvent;
import bio.terra.pearl.core.service.workflow.EventService;
import bio.terra.pearl.core.service.workflow.ParticipantTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.util.List;

/** Holds logic for updating whether or not an enrollee is consented */
@Service
@Slf4j
public class ConsentStatusProcessor {
    private final ParticipantTaskService participantTaskService;
    private final EnrolleeService enrolleeService;
    private final EventService eventService;

    public ConsentStatusProcessor(ParticipantTaskService participantTaskService,
                                  EnrolleeService enrolleeService,
                                  EventService eventService) {
        this.participantTaskService = participantTaskService;
        this.enrolleeService = enrolleeService;
        this.eventService = eventService;
    }

    /**
     * Listen for survey events, and if the event was consent-related, update the enrollee's consent status
     */
    @EventListener
    @Order(DispatcherOrder.CONSENT_PROCESSOR)
    public void handleEvent(EnrolleeSurveyEvent enrolleeEvent) {
        /** for now, only updates to consent tasks have the possibility to update consent status */
        if (enrolleeEvent.getParticipantTask().getTaskType() == TaskType.CONSENT) {
            updateEnrolleeConsented(enrolleeEvent.getEnrollee(), enrolleeEvent.getEnrollee().getParticipantTasks(), enrolleeEvent);
        }
    }

    /** check the enrollee's current consent status, and update it if needed.  this will handle both
     * updating the DB and the enrollee object in-place */
    public Enrollee updateEnrolleeConsented(Enrollee enrollee, List<ParticipantTask> participantTasks, EnrolleeEvent enrolleeEvent) {
        boolean consentStatus = checkIsEnrolleeConsented(participantTasks);
        if (enrollee.isConsented() != consentStatus) {
            enrollee.setConsented(consentStatus);
            enrolleeService.updateConsented(enrollee.getId(), consentStatus);
            if (enrollee.isConsented()) {
                eventService.publishEnrolleeConsentEvent(enrollee, enrolleeEvent.getPortalParticipantUser());
            }
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
