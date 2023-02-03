package bio.terra.pearl.core.service.consent;

import bio.terra.pearl.core.model.consent.ConsentForm;
import bio.terra.pearl.core.model.consent.StudyEnvironmentConsent;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import bio.terra.pearl.core.model.workflow.TaskType;
import bio.terra.pearl.core.service.participant.ParticipantTaskService;
import bio.terra.pearl.core.service.rule.EnrolleeRuleData;
import bio.terra.pearl.core.service.rule.RuleEvaluator;
import bio.terra.pearl.core.service.workflow.EnrolleeCreationEvent;
import bio.terra.pearl.core.service.study.StudyEnvironmentConsentService;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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

    public ConsentTaskDispatcher(StudyEnvironmentConsentService studyEnvironmentConsentService,
                                 ParticipantTaskService participantTaskService) {
        this.studyEnvironmentConsentService = studyEnvironmentConsentService;
        this.participantTaskService = participantTaskService;
    }

    @EventListener
    @Order(1) // consent tasks get first priority over other tasks, so add them first
    public void createConsentTasks(EnrolleeCreationEvent enrolleeEvent) {
        List<StudyEnvironmentConsent> studyEnvConsents = studyEnvironmentConsentService
                .findAllByStudyEnvIdWithConsent(enrolleeEvent.getStudyEnvironment().getId());
        List<ParticipantTask> consentTasks = buildConsentTasks(enrolleeEvent, enrolleeEvent.getEnrolleeRuleData(),
                studyEnvConsents);
        for (ParticipantTask task : consentTasks) {
            logger.info("Task creation: enrollee {}  -- task {}, target {}", enrolleeEvent.getEnrollee().getShortcode(),
                    task.getTaskType(), task.getTargetStableId());
            task = participantTaskService.create(task);
            enrolleeEvent.getEnrollee().getParticipantTasks().add(task);
        }
    }

    /** builds the consent tasks, does not add them to the event or persist them */
    public List<ParticipantTask> buildConsentTasks(EnrolleeCreationEvent enrolleeEvent,
                                                   EnrolleeRuleData enrolleeRuleData,
                                                   List<StudyEnvironmentConsent> studyEnvConsents) {
        List<ParticipantTask> consentTasks = new ArrayList<>();
        for (StudyEnvironmentConsent studyConsent : studyEnvConsents) {
            if (RuleEvaluator.evaluateEnrolleeRule(studyConsent.getEligibilityRule(), enrolleeRuleData)) {
                ParticipantTask consentTask = buildConsentTask(studyConsent, enrolleeEvent);
                if (!isDuplicateTask(consentTask, enrolleeEvent.getEnrollee().getParticipantTasks())) {
                    consentTasks.add(consentTask);
                }
            }
        }
        return consentTasks;
    }



    /** builds a task for the given consent form -- does NOT evaulate the rule */
    public ParticipantTask buildConsentTask(StudyEnvironmentConsent studyConsent,
                                            EnrolleeCreationEvent enrolleeEvent) {
        ConsentForm consentForm = studyConsent.getConsentForm();
        ParticipantTask consentTask = ParticipantTask.builder()
                .enrolleeId(enrolleeEvent.getEnrollee().getId())
                .portalParticipantUserId(enrolleeEvent.getPortalParticipantUser().getId())
                .studyEnvironmentId(enrolleeEvent.getStudyEnvironment().getId())
                .blocksHub(true) // by default, all consent tasks currently stop a user from entering the hub
                .taskOrder(studyConsent.getConsentOrder())
                .targetStableId(consentForm.getStableId())
                .targetAssignedVersion(consentForm.getVersion())
                .taskType(TaskType.CONSENT)
                .targetName(consentForm.getName())
                .status(TaskStatus.NEW.name())
                .build();
        return consentTask;
    }

    /**
     * since we're using an event-based task creation system, and we don't care too much about performance,
     * it is probably a good idea to check for duplicates before adding a new task, to ensure, e.g., that
     * a participant doesn't get assigned two different tasks to complete the same survey
     */
    public boolean isDuplicateTask(ParticipantTask task, Set<ParticipantTask> allTasks) {
        // TODO implement for real
        return false;
    }
}
