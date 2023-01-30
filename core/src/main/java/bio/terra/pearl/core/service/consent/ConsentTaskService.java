package bio.terra.pearl.core.service.consent;

import bio.terra.pearl.core.model.consent.ConsentForm;
import bio.terra.pearl.core.model.consent.StudyEnvironmentConsent;
import bio.terra.pearl.core.model.participant.ParticipantTask;
import bio.terra.pearl.core.model.participant.TaskStatus;
import bio.terra.pearl.core.model.participant.TaskType;
import bio.terra.pearl.core.service.rule.EnrolleeRuleData;
import bio.terra.pearl.core.service.rule.RuleEvaluator;
import bio.terra.pearl.core.service.study.EnrolleeCreationEvent;
import bio.terra.pearl.core.service.study.StudyEnvironmentConsentService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

/** Holds logic for building and processing consent tasks */
@Service
public class ConsentTaskService {
    private StudyEnvironmentConsentService studyEnvironmentConsentService;

    public ConsentTaskService(StudyEnvironmentConsentService studyEnvironmentConsentService) {
        this.studyEnvironmentConsentService = studyEnvironmentConsentService;
    }


    public List<ParticipantTask> buildConsentTasks(EnrolleeCreationEvent enrolleeEvent,
                                                   List<ParticipantTask> allTasks,
                                                   EnrolleeRuleData enrolleeRuleData) {
        List<StudyEnvironmentConsent> studyEnvConsents = studyEnvironmentConsentService
                .findAllByStudyEnvIdWithConsent(enrolleeEvent.getStudyEnvironment().getId());
        return buildConsentTasks(enrolleeEvent, allTasks, enrolleeRuleData, studyEnvConsents);
    }

    /** makes consent tasks, but does not persist them */
    public List<ParticipantTask> buildConsentTasks(EnrolleeCreationEvent enrolleeEvent,
                                                   List<ParticipantTask> allTasks,
                                                   EnrolleeRuleData enrolleeRuleData,
                                                   List<StudyEnvironmentConsent> studyEnvConsents) {
        List<ParticipantTask> consentTasks = new ArrayList<>();
        for (StudyEnvironmentConsent studyConsent : studyEnvConsents) {
            if (RuleEvaluator.evaluateEnrolleeRule(studyConsent.getEligibilityRule(), enrolleeRuleData)) {
                ParticipantTask consentTask = buildConsentTask(studyConsent, enrolleeEvent);
                if (!isDuplicateTask(consentTask, allTasks)) {
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
    public boolean isDuplicateTask(ParticipantTask task, List<ParticipantTask> allTasks) {
        // TODO implement for real
        return false;
    }
}
