package bio.terra.pearl.core.service.survey;

import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import bio.terra.pearl.core.model.workflow.TaskType;
import bio.terra.pearl.core.service.workflow.ParticipantTaskService;
import bio.terra.pearl.core.service.rule.EnrolleeRuleData;
import bio.terra.pearl.core.service.rule.RuleEvaluator;
import bio.terra.pearl.core.service.study.StudyEnvironmentSurveyService;
import bio.terra.pearl.core.service.workflow.DispatcherOrder;
import bio.terra.pearl.core.service.workflow.EnrolleeEvent;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

/** listens for events and updates enrollee survey tasks accordingly */
@Service
public class SurveyTaskDispatcher {
    private static final Logger logger = LoggerFactory.getLogger(SurveyTaskDispatcher.class);
    private StudyEnvironmentSurveyService studyEnvironmentSurveyService;
    private ParticipantTaskService participantTaskService;

    public SurveyTaskDispatcher(StudyEnvironmentSurveyService studyEnvironmentSurveyService,
                                 ParticipantTaskService participantTaskService) {
        this.studyEnvironmentSurveyService = studyEnvironmentSurveyService;
        this.participantTaskService = participantTaskService;
    }

    /** survey tasks could be triggered by just about anything, so listen to all enrollee events */
    @EventListener
    @Order(DispatcherOrder.SURVEY)
    public void createSurveyTasks(EnrolleeEvent enrolleeEvent) {
        List<StudyEnvironmentSurvey> studyEnvSurveys = studyEnvironmentSurveyService
                .findAllByStudyEnvIdWithSurvey(enrolleeEvent.getEnrollee().getStudyEnvironmentId());
        List<ParticipantTask> tasksToAdd = buildTasks(enrolleeEvent.getEnrollee(),
                enrolleeEvent.getPortalParticipantUser(),
                enrolleeEvent.getEnrolleeRuleData(),
                studyEnvSurveys);
        for (ParticipantTask task : tasksToAdd) {
            logger.info("Task creation: enrollee {}  -- task {}, target {}", enrolleeEvent.getEnrollee().getShortcode(),
                    task.getTaskType(), task.getTargetStableId());
            task = participantTaskService.create(task);
            enrolleeEvent.getEnrollee().getParticipantTasks().add(task);
        }
    }

    /** builds the consent tasks, does not add them to the event or persist them */
    public List<ParticipantTask> buildTasks(Enrollee enrollee,
                                            PortalParticipantUser portalParticipantUser,
                                            EnrolleeRuleData enrolleeRuleData,
                                            List<StudyEnvironmentSurvey> studyEnvSurveys) {
        List<ParticipantTask> tasks = new ArrayList<>();
        for (StudyEnvironmentSurvey studySurvey : studyEnvSurveys) {
            if (isEligibleForSurvey(studySurvey.getEligibilityRule(), enrolleeRuleData)) {
                ParticipantTask task = buildTask(studySurvey, enrollee, portalParticipantUser);
                if (!isDuplicateTask(studySurvey, task, enrollee.getParticipantTasks())) {
                    tasks.add(task);
                }
            }
        }
        return tasks;
    }

    public static boolean isEligibleForSurvey(String eligibilityRule, EnrolleeRuleData enrolleeRuleData) {
        return RuleEvaluator.evaluateEnrolleeRule(eligibilityRule, enrolleeRuleData);
    }

    /** builds a task for the given survey -- does NOT evaluate the rule */
    public ParticipantTask buildTask(StudyEnvironmentSurvey studySurvey,
                                     Enrollee enrollee, PortalParticipantUser portalParticipantUser) {
        Survey survey = studySurvey.getSurvey();
        ParticipantTask task = ParticipantTask.builder()
                .enrolleeId(enrollee.getId())
                .portalParticipantUserId(portalParticipantUser.getId())
                .studyEnvironmentId(enrollee.getStudyEnvironmentId())
                .blocksHub(studySurvey.isRequired())
                .taskOrder(studySurvey.getSurveyOrder())
                .targetStableId(survey.getStableId())
                .targetAssignedVersion(survey.getVersion())
                .taskType(TaskType.SURVEY)
                .targetName(survey.getName())
                .status(TaskStatus.NEW)
                .build();
        return task;
    }

    /**
     * To avoid accidentally assigning the same survey to a participant multiple times, confirm that
     * if the stableId matches an existing task, the existing task must be complete and the configured
     * survey must allow recurrence.
     */
    public static boolean isDuplicateTask(StudyEnvironmentSurvey studySurvey, ParticipantTask task,
                                   Set<ParticipantTask> allTasks) {
        return !allTasks.stream().filter(existingTask ->
                existingTask.getTargetStableId().equals(task.getTargetStableId()) &&
                        !isRecurrenceWindowOpen(studySurvey, existingTask))
                .toList().isEmpty();
    }



    /**
     * whether or not sufficient time has passed since a previous instance of a survey being assigned to assign
     * a new one
     */
    public static boolean isRecurrenceWindowOpen(StudyEnvironmentSurvey studySurvey, ParticipantTask pastTask) {
        if (!studySurvey.isRecur()) {
            return false;
        }
        Instant pastCutoffTime = ZonedDateTime.now(ZoneOffset.UTC)
                .minusDays(studySurvey.getRecurrenceIntervalDays() - RECUR_TASK_BUFFER_DAYS).toInstant();
        return pastTask.getCreatedAt().isBefore(pastCutoffTime);
    }

    /**
     * this is a fudge factor in our logic to prevent duplicate survey assignments -- we don't want to
     * allow duplicate tasks assigned, but we don't want to prevent tasks from being assigned just because
     * of time zone fuzziness, leap years, etc...
     */
    public static final int RECUR_TASK_BUFFER_DAYS = 3;
}
