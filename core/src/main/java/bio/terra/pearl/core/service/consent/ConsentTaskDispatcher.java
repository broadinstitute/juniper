package bio.terra.pearl.core.service.consent;

import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.consent.ConsentForm;
import bio.terra.pearl.core.model.consent.StudyEnvironmentConsent;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import bio.terra.pearl.core.model.workflow.TaskType;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.rule.EnrolleeContext;
import bio.terra.pearl.core.service.search.EnrolleeSearchContext;
import bio.terra.pearl.core.service.search.EnrolleeSearchExpressionParser;
import bio.terra.pearl.core.service.study.StudyEnvironmentConsentService;
import bio.terra.pearl.core.service.workflow.DispatcherOrder;
import bio.terra.pearl.core.service.workflow.EnrolleeCreationEvent;
import bio.terra.pearl.core.service.workflow.EnrolleeEvent;
import bio.terra.pearl.core.service.workflow.ParticipantTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Holds logic for building and processing consent tasks, and event listeners for triggering updates */
@Service
@Slf4j
public class ConsentTaskDispatcher {
    private StudyEnvironmentConsentService studyEnvironmentConsentService;
    private ParticipantTaskService participantTaskService;
    private EnrolleeService enrolleeService;
    private EnrolleeSearchExpressionParser enrolleeSearchExpressionParser;

    public ConsentTaskDispatcher(StudyEnvironmentConsentService studyEnvironmentConsentService,
                                 ParticipantTaskService participantTaskService,
                                 EnrolleeService enrolleeService,
                                 EnrolleeSearchExpressionParser enrolleeSearchExpressionParser) {
        this.studyEnvironmentConsentService = studyEnvironmentConsentService;
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
    public void handleEvent(EnrolleeCreationEvent enrolleeEvent) {
        updateConsentTasks(enrolleeEvent);
    }

    @EventListener
    @Order(DispatcherOrder.CONSENT_TASK)
    public void handleEvent(EnrolleeConsentEvent enrolleeEvent) {
        updateConsentTasks(enrolleeEvent);
    }

    public void updateConsentTasks(EnrolleeEvent enrolleeEvent) {
        List<StudyEnvironmentConsent> studyEnvConsents = studyEnvironmentConsentService
                .findAllByStudyEnvIdWithConsent(enrolleeEvent.getEnrollee().getStudyEnvironmentId());
        List<ParticipantTask> tasks = buildTasks(enrolleeEvent.getEnrollee(), enrolleeEvent.getEnrolleeContext(),
                enrolleeEvent.getPortalParticipantUser().getId(),
                studyEnvConsents);
        DataAuditInfo auditInfo = DataAuditInfo.builder()
                .systemProcess(DataAuditInfo.systemProcessName(getClass(), "updateConsentTasks"))
                .portalParticipantUserId(enrolleeEvent.getPortalParticipantUser().getId())
                .enrolleeId(enrolleeEvent.getEnrollee().getId()).build();
        for (ParticipantTask task : tasks) {
            task = participantTaskService.create(task, auditInfo);
            enrolleeEvent.getEnrollee().getParticipantTasks().add(task);
        }
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


    /** builds the consent tasks, does not add them to the event or persist them */
    public List<ParticipantTask> buildTasks(Enrollee enrollee,
                                            EnrolleeContext enrolleeContext,
                                                   UUID portalParticipantUserId,
                                                   List<StudyEnvironmentConsent> studyEnvConsents) {
        List<ParticipantTask> tasks = new ArrayList<>();
        for (StudyEnvironmentConsent studyConsent : studyEnvConsents) {
            // TODO JN-977: this logic will need to change because we might need to support consents for proxies
            if (enrollee.isSubject() && enrolleeSearchExpressionParser
                    .parseRule(studyConsent.getEligibilityRule())
                    .evaluate(new EnrolleeSearchContext(enrolleeContext.getEnrollee(), enrolleeContext.getProfile()))) {
                ParticipantTask consentTask = buildTask(studyConsent, enrollee, portalParticipantUserId);
                if (!isDuplicateTask(consentTask, enrollee.getParticipantTasks())) {
                    tasks.add(consentTask);
                }
            }
        }
        return tasks;
    }



    /** builds a task for the given consent form -- does NOT evaulate the rule */
    public ParticipantTask buildTask(StudyEnvironmentConsent studyConsent,
                                            Enrollee enrollee, UUID portalParticipantUserId) {
        ConsentForm consentForm = studyConsent.getConsentForm();
        ParticipantTask task = ParticipantTask.builder()
                .enrolleeId(enrollee.getId())
                .portalParticipantUserId(portalParticipantUserId)
                .studyEnvironmentId(enrollee.getStudyEnvironmentId())
                .blocksHub(studyConsent.isStudyRequired())
                .taskOrder(studyConsent.getConsentOrder())
                .targetStableId(consentForm.getStableId())
                .targetAssignedVersion(consentForm.getVersion())
                .taskType(TaskType.CONSENT)
                .targetName(consentForm.getName())
                .status(TaskStatus.NEW)
                .build();
        return task;
    }

    /**
     * an enrollee cannot have two tasks to complete the same consent form (defined by stableId)
     */
    public boolean isDuplicateTask(ParticipantTask task, List<ParticipantTask> allTasks) {
        return !allTasks.stream().filter(existingTask ->
                        existingTask.getTargetStableId().equals(task.getTargetStableId()))
                .toList().isEmpty();
    }
}
