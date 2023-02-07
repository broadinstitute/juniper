package bio.terra.pearl.core.service.consent;

import bio.terra.pearl.core.model.consent.ConsentForm;
import bio.terra.pearl.core.model.consent.StudyEnvironmentConsent;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import bio.terra.pearl.core.model.workflow.TaskType;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.ParticipantTaskService;
import bio.terra.pearl.core.service.rule.EnrolleeRuleData;
import bio.terra.pearl.core.service.rule.RuleEvaluator;
import bio.terra.pearl.core.service.study.StudyEnvironmentConsentService;
import bio.terra.pearl.core.service.workflow.DispatcherOrder;
import bio.terra.pearl.core.service.workflow.EnrolleeEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

/** Holds logic for building and processing consent tasks */
@Service
public class ConsentTaskDispatcher {
    private static final Logger logger = LoggerFactory.getLogger(ConsentTaskDispatcher.class);
    private StudyEnvironmentConsentService studyEnvironmentConsentService;
    private ParticipantTaskService participantTaskService;
    private EnrolleeService enrolleeService;

    public ConsentTaskDispatcher(StudyEnvironmentConsentService studyEnvironmentConsentService,
                                 ParticipantTaskService participantTaskService, EnrolleeService enrolleeService) {
        this.studyEnvironmentConsentService = studyEnvironmentConsentService;
        this.participantTaskService = participantTaskService;
        this.enrolleeService = enrolleeService;
    }

    @EventListener
    @Order(DispatcherOrder.CONSENT) // consent tasks get first priority over other tasks, so add them first
    public void createTasks(EnrolleeEvent enrolleeEvent) {
        List<StudyEnvironmentConsent> studyEnvConsents = studyEnvironmentConsentService
                .findAllByStudyEnvIdWithConsent(enrolleeEvent.getEnrollee().getStudyEnvironmentId());
        List<ParticipantTask> tasks = buildTasks(enrolleeEvent.getEnrollee(), enrolleeEvent.getEnrolleeRuleData(),
                enrolleeEvent.getPortalParticipantUser().getId(),
                studyEnvConsents);
        for (ParticipantTask task : tasks) {
            logger.info("Task creation: enrollee {}  -- task {}, target {}", enrolleeEvent.getEnrollee().getShortcode(),
                    task.getTaskType(), task.getTargetStableId());
            task = participantTaskService.create(task);
            enrolleeEvent.getEnrollee().getParticipantTasks().add(task);
        }
        updateEnrolleeConsented(enrolleeEvent.getEnrollee(), enrolleeEvent.getEnrollee().getParticipantTasks());
    }

    /** check the enrollee's current consent status, and update it if needed.  this will handle both
     * updating the DB and the enrollee object in-place */
    public Enrollee updateEnrolleeConsented(Enrollee enrollee, Set<ParticipantTask> participantTasks) {
        boolean consentStatus = checkIsEnrolleeConsented(participantTasks);
        if (enrollee.isConsented() != consentStatus) {
            enrollee.setConsented(consentStatus);
            enrolleeService.updateConsented(enrollee.getId(), consentStatus);
        }
        return enrollee;
    }

    /** an enrollee is consented iff they have at least one completed consent and no outstanding consents */
    public static boolean checkIsEnrolleeConsented(Set<ParticipantTask> participantTasks) {
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
                                                   EnrolleeRuleData enrolleeRuleData,
                                                   UUID portalParticipantUserId,
                                                   List<StudyEnvironmentConsent> studyEnvConsents) {
        List<ParticipantTask> tasks = new ArrayList<>();
        for (StudyEnvironmentConsent studyConsent : studyEnvConsents) {
            if (RuleEvaluator.evaluateEnrolleeRule(studyConsent.getEligibilityRule(), enrolleeRuleData)) {
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
    public boolean isDuplicateTask(ParticipantTask task, Set<ParticipantTask> allTasks) {
        return !allTasks.stream().filter(existingTask ->
                        existingTask.getTargetStableId().equals(task.getTargetStableId()))
                .toList().isEmpty();
    }
}
