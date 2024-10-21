package bio.terra.pearl.core.service.survey;

import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.audit.ResponsibleEntity;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.RecurrenceType;
import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.survey.SurveyType;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import bio.terra.pearl.core.model.workflow.TaskType;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import bio.terra.pearl.core.service.rule.EnrolleeContext;
import bio.terra.pearl.core.service.rule.EnrolleeContextService;
import bio.terra.pearl.core.service.search.EnrolleeSearchContext;
import bio.terra.pearl.core.service.search.EnrolleeSearchExpressionParser;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.study.StudyEnvironmentSurveyService;
import bio.terra.pearl.core.service.survey.event.EnrolleeSurveyEvent;
import bio.terra.pearl.core.service.survey.event.SurveyPublishedEvent;
import bio.terra.pearl.core.service.workflow.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/** listens for events and updates enrollee survey tasks accordingly */
@Service
@Slf4j
public class SurveyTaskDispatcher {
    private final StudyEnvironmentSurveyService studyEnvironmentSurveyService;
    private final StudyEnvironmentService studyEnvironmentService;
    private final ParticipantTaskService participantTaskService;
    private final EnrolleeService enrolleeService;
    private final PortalParticipantUserService portalParticipantUserService;
    private final EnrolleeContextService enrolleeContextService;
    private final EnrolleeSearchExpressionParser enrolleeSearchExpressionParser;


    public SurveyTaskDispatcher(StudyEnvironmentSurveyService studyEnvironmentSurveyService,
                                StudyEnvironmentService studyEnvironmentService, ParticipantTaskService participantTaskService,
                                EnrolleeService enrolleeService,
                                PortalParticipantUserService portalParticipantUserService,
                                EnrolleeContextService enrolleeContextService,
                                EnrolleeSearchExpressionParser enrolleeSearchExpressionParser) {
        this.studyEnvironmentSurveyService = studyEnvironmentSurveyService;
        this.studyEnvironmentService = studyEnvironmentService;
        this.participantTaskService = participantTaskService;
        this.enrolleeService = enrolleeService;
        this.portalParticipantUserService = portalParticipantUserService;
        this.enrolleeContextService = enrolleeContextService;
        this.enrolleeSearchExpressionParser = enrolleeSearchExpressionParser;
    }

    public void assignScheduledSurveys() {
        List<StudyEnvironment> studyEnvironments = studyEnvironmentService.findAll();
        for (StudyEnvironment studyEnv : studyEnvironments) {
            assignScheduledSurveys(studyEnv);
        }
    }

    public void assignScheduledSurveys(StudyEnvironment studyEnv) {
        List<StudyEnvironmentSurvey> studyEnvSurveys = studyEnvironmentSurveyService.findAllByStudyEnvIdWithSurveyNoContent(studyEnv.getId(), true);
        for (StudyEnvironmentSurvey studyEnvSurvey : studyEnvSurveys) {
            Survey survey = studyEnvSurvey.getSurvey();
            if (survey.getRecurrenceType() != RecurrenceType.NONE && survey.getRecurrenceIntervalDays() != null) {
                assignRecurringSurvey(studyEnvSurvey);
            }
            if (survey.getDaysAfterEligible() != null && survey.getDaysAfterEligible() > 0) {
                assignDelayedSurvey(studyEnvSurvey);
            }
        }
    }

    /** will assign a recurringsurvey to enrollees who have already taken it at least once, but are due to take it again */
    public void assignRecurringSurvey(StudyEnvironmentSurvey studyEnvSurvey) {
        List<Enrollee> enrollees = enrolleeService.findWithTaskInPast(
                studyEnvSurvey.getStudyEnvironmentId(),
                studyEnvSurvey.getSurvey().getStableId(),
                Duration.of(studyEnvSurvey.getSurvey().getRecurrenceIntervalDays(), ChronoUnit.DAYS));
        assign(enrollees, studyEnvSurvey, false, new ResponsibleEntity(DataAuditInfo.systemProcessName(getClass(), "assignRecurringSurvey")));
    }

    /** will assign a delayed survey to enrollees who have never taken it, but are due to take it now */
    public void assignDelayedSurvey(StudyEnvironmentSurvey studyEnvSurvey) {
        List<Enrollee> enrollees = enrolleeService.findUnassignedToTask(studyEnvSurvey.getStudyEnvironmentId(), studyEnvSurvey.getSurvey().getStableId(), null);
        enrollees = enrollees.stream().filter(enrollee ->
                enrollee.getCreatedAt().plus(studyEnvSurvey.getSurvey().getDaysAfterEligible(), ChronoUnit.DAYS)
                        .isBefore(Instant.now())).toList();
        assign(enrollees, studyEnvSurvey, false, new ResponsibleEntity(DataAuditInfo.systemProcessName(getClass(), "assignDelayedSurvey")));
    }

    public List<ParticipantTask> assign(ParticipantTaskAssignDto assignDto,
                                        UUID studyEnvironmentId,
                                        ResponsibleEntity operator) {
        List<Enrollee> enrollees = findMatchingEnrollees(assignDto, studyEnvironmentId);
        StudyEnvironmentSurvey studyEnvironmentSurvey = studyEnvironmentSurveyService
                .findAllWithSurveyNoContent(List.of(studyEnvironmentId), assignDto.targetStableId(), true)
                .stream().findFirst().orElseThrow(() -> new NotFoundException("Could not find active survey to assign tasks"));
        return assign(enrollees, studyEnvironmentSurvey, assignDto.overrideEligibility(), operator);
    }

    public List<ParticipantTask> assign(List<Enrollee> enrollees,
                                        StudyEnvironmentSurvey studyEnvironmentSurvey,
                                        boolean overrideEligibility,
                                        ResponsibleEntity operator) {
        List<UUID> profileIds = enrollees.stream().map(Enrollee::getProfileId).toList();
        List<PortalParticipantUser> ppUsers = portalParticipantUserService.findByProfileIds(profileIds);
        if (ppUsers.size() != enrollees.size()) {
            throw new IllegalStateException("Task dispatch failed: Portal participant user not matched to enrollee");
        }
        List<EnrolleeContext> enrolleeRuleData = enrolleeContextService.fetchData(enrollees.stream().map(Enrollee::getId).toList());

        UUID auditOperationId = UUID.randomUUID();
        List<ParticipantTask> createdTasks = new ArrayList<>();
        for (int i = 0; i < enrollees.size(); i++) {
            List<ParticipantTask> existingTasks = participantTaskService.findByEnrolleeId(enrollees.get(i).getId());
            Optional<ParticipantTask> taskOpt;
            if (overrideEligibility) {
                taskOpt = Optional.of(buildTask(enrollees.get(i), ppUsers.get(i),
                        studyEnvironmentSurvey, studyEnvironmentSurvey.getSurvey()));
            } else {
                taskOpt = buildTaskIfApplicable(enrollees.get(i), existingTasks, ppUsers.get(i), enrolleeRuleData.get(i),
                        studyEnvironmentSurvey, studyEnvironmentSurvey.getSurvey());
            }
            if (taskOpt.isPresent()) {
                ParticipantTask task = taskOpt.get();
                copyForwardResponseIfApplicable(task, studyEnvironmentSurvey.getSurvey(), existingTasks);
                DataAuditInfo auditInfo = DataAuditInfo.builder()
                        .portalParticipantUserId(ppUsers.get(i).getId())
                        .operationId(auditOperationId)
                        .enrolleeId(enrollees.get(i).getId()).build();
                auditInfo.setResponsibleEntity(operator);

                task = participantTaskService.create(task, auditInfo);
                log.info("Task creation: enrollee {}  -- task {}, target {}", enrollees.get(i).getShortcode(),
                        task.getTaskType(), task.getTargetStableId());
                createdTasks.add(task);
            }
        }
        return createdTasks;
    }

    /**
     * depending on recurrence type, will copy forward a past response so that updates are merged.
     * If we later support the 'prepopulate' option, this would be where would we clone the prior response
     * */
    protected void copyForwardResponseIfApplicable(ParticipantTask task, Survey survey, List<ParticipantTask> existingTasks) {
        if (survey.getRecurrenceType().equals(RecurrenceType.UPDATE)) {
            Optional<ParticipantTask> existingTask = existingTasks.stream()
                    .filter(t -> t.getTargetStableId().equals(task.getTargetStableId()))
                    .max(Comparator.comparing(ParticipantTask::getCreatedAt));
            existingTask.ifPresent(participantTask -> task.setSurveyResponseId(participantTask.getSurveyResponseId()));
        }
    }

    protected List<Enrollee> findMatchingEnrollees(ParticipantTaskAssignDto assignDto,
                                                   UUID studyEnvironmentId) {
        if (assignDto.assignAllUnassigned()
                && (Objects.isNull(assignDto.enrolleeIds()) || assignDto.enrolleeIds().isEmpty())) {
            return enrolleeService.findUnassignedToTask(studyEnvironmentId,
                    assignDto.targetStableId(), null);
        } else {
            return enrolleeService.findAll(assignDto.enrolleeIds());
        }
    }

    /** create the survey tasks for an enrollee's initial creation */
    @EventListener
    @Order(DispatcherOrder.SURVEY_TASK)
    public void updateSurveyTasksForNewEnrollee(EnrolleeCreationEvent enrolleeEvent) {
        updateSurveyTasks(enrolleeEvent);
    }

    /** survey responses can update what surveys a person is eligible for -- recompute as needed */
    @EventListener
    @Order(DispatcherOrder.SURVEY_TASK)
    public void updateSurveyTasksFromSurvey(EnrolleeSurveyEvent enrolleeEvent) {
        /** for now, only recompute on updates involving a completed survey.  This will
         * avoid assigning surveys based on an answer that was quickly changed, since we don't
         * yet have functions for unassigning surveys */
        if (!enrolleeEvent.getSurveyResponse().isComplete()) {
            return;
        }
        updateSurveyTasks(enrolleeEvent);
    }

    protected void updateSurveyTasks(EnrolleeEvent enrolleeEvent) {
        DataAuditInfo auditInfo = DataAuditInfo.builder()
                .systemProcess(getClass().getSimpleName() + ".updateSurveyTasks")
                .portalParticipantUserId(enrolleeEvent.getPortalParticipantUser().getId())
                .enrolleeId(enrolleeEvent.getEnrollee().getId()).build();

        List<StudyEnvironmentSurvey> studyEnvSurveys = studyEnvironmentSurveyService
                .findAllByStudyEnvIdWithSurveyNoContent(enrolleeEvent.getEnrollee().getStudyEnvironmentId(), true);

        for (StudyEnvironmentSurvey studyEnvSurvey: studyEnvSurveys) {
            if (studyEnvSurvey.getSurvey().isAutoAssign()) {
                createTaskIfApplicable(studyEnvSurvey, enrolleeEvent, auditInfo);
            }
        }
    }


    private void createTaskIfApplicable(StudyEnvironmentSurvey studyEnvSurvey, EnrolleeEvent event, DataAuditInfo auditInfo) {
        Optional<ParticipantTask> taskOpt = buildTaskIfApplicable(event.getEnrollee(),
                event.getEnrollee().getParticipantTasks(),
                event.getPortalParticipantUser(), event.getEnrolleeContext(),
                studyEnvSurvey, studyEnvSurvey.getSurvey());
        if (taskOpt.isPresent()) {
            ParticipantTask task = taskOpt.get();
            log.info("Task creation: enrollee {}  -- task {}, target {}", event.getEnrollee().getShortcode(),
                    task.getTaskType(), task.getTargetStableId());
            task = participantTaskService.create(task, auditInfo);
            event.getEnrollee().getParticipantTasks().add(task);
        }
    }

    @EventListener
    @Order(DispatcherOrder.SURVEY_TASK)
    public void handleSurveyPublished(SurveyPublishedEvent event) {
        if (event.getSurvey().isAutoUpdateTaskAssignments()) {
            ParticipantTaskUpdateDto updateDto = new ParticipantTaskUpdateDto(
                    List.of(new ParticipantTaskUpdateDto.TaskUpdateSpec(
                            event.getSurvey().getStableId(),
                            event.getSurvey().getVersion(),
                            null,
                            null)),
                    null,
                    true
                    );
            participantTaskService.updateTasks(
                    event.getStudyEnvironmentId(),
                    updateDto,
                    new ResponsibleEntity(DataAuditInfo.systemProcessName(getClass(), "handleSurveyPublished.autoUpdateTaskAssignments"))
            );
        }
        if (event.getSurvey().isAssignToExistingEnrollees()) {
            ParticipantTaskAssignDto assignDto = new ParticipantTaskAssignDto(
                    taskTypeForSurveyType.get(event.getSurvey().getSurveyType()),
                    event.getSurvey().getStableId(),
                    event.getSurvey().getVersion(),
                null,
                    true,
                    false);

            assign(assignDto, event.getStudyEnvironmentId(),
                    new ResponsibleEntity(DataAuditInfo.systemProcessName(getClass(), "handleSurveyPublished.assignToExistingEnrollees")));

        }
    }


    /** builds any survey tasks that the enrollee is eligible for that are not duplicates
     *  Does not add them to the event or persist them.
     *  */
    public Optional<ParticipantTask> buildTaskIfApplicable(Enrollee enrollee,
                                                      List<ParticipantTask> existingEnrolleeTasks,
                                                      PortalParticipantUser portalParticipantUser,
                                                           EnrolleeContext enrolleeContext,
                                                      StudyEnvironmentSurvey studyEnvSurvey, Survey survey) {
        if (isEligibleForSurvey(survey, enrolleeContext)) {
            ParticipantTask task = buildTask(enrollee, portalParticipantUser, studyEnvSurvey, studyEnvSurvey.getSurvey());
            if (!isDuplicateTask(studyEnvSurvey, task, existingEnrolleeTasks)) {
                return Optional.of(task);
            }
        }
        return Optional.empty();
    }

    public boolean isEligibleForSurvey(Survey survey, EnrolleeContext enrolleeContext) {
        /**
         * eligible if the enrollee is a subject, the survey is not restricted by time, and the enrollee meets the rule
         * note that this does not include a duplicate task check -- that is done elsewhere
         */
        // TODO JN-977: this logic will need to change because we will need to support surveys for proxies
        return enrolleeContext.getEnrollee().isSubject() &&
                (survey.getDaysAfterEligible() == null ||
                        enrolleeContext.getEnrollee().getCreatedAt().plus(survey.getDaysAfterEligible(), ChronoUnit.DAYS).isBefore(Instant.now())) &&
                enrolleeSearchExpressionParser
                .parseRule(survey.getEligibilityRule())
                .evaluate(new EnrolleeSearchContext(enrolleeContext.getEnrollee(), enrolleeContext.getProfile()));
    }

    /** builds a task for the given survey -- does NOT evaluate the rule or check duplicates */
    public ParticipantTask buildTask(Enrollee enrollee, PortalParticipantUser portalParticipantUser,
                                     StudyEnvironmentSurvey studyEnvSurvey, Survey survey) {
        if (!studyEnvSurvey.getSurveyId().equals(survey.getId())) {
            throw new IllegalArgumentException("Survey does not match StudyEnvironmentSurvey");
        }
        TaskType taskType = taskTypeForSurveyType.get(survey.getSurveyType());
        ParticipantTask task = ParticipantTask.builder()
                .enrolleeId(enrollee.getId())
                .portalParticipantUserId(portalParticipantUser.getId())
                .studyEnvironmentId(enrollee.getStudyEnvironmentId())
                .blocksHub(survey.isRequired())
                .taskOrder(studyEnvSurvey.getSurveyOrder())
                .targetStableId(survey.getStableId())
                .targetAssignedVersion(survey.getVersion())
                .taskType(taskType)
                .targetName(survey.getName())
                .status(TaskStatus.NEW)
                .build();
        return task;
    }

    private final Map<SurveyType, TaskType> taskTypeForSurveyType = Map.of(
            SurveyType.CONSENT, TaskType.CONSENT,
            SurveyType.RESEARCH, TaskType.SURVEY,
            SurveyType.OUTREACH, TaskType.OUTREACH,
            SurveyType.ADMIN, TaskType.ADMIN_FORM
    );

    /**
     * To avoid accidentally assigning the same survey or outreach activity to a participant multiple times,
     * confirm that if the stableId matches an existing task, the existing task must be complete and the
     * configured survey must allow recurrence.
     */
    public static boolean isDuplicateTask(StudyEnvironmentSurvey studySurvey, ParticipantTask task,
                                   List<ParticipantTask> allTasks) {
        return !allTasks.stream()
                .filter(existingTask ->
                        existingTask.getTaskType().equals(task.getTaskType()) &&
                        existingTask.getTargetStableId().equals(task.getTargetStableId()) &&
                        !isRecurrenceWindowOpen(studySurvey, existingTask))
                .toList().isEmpty();
    }

    /**
     * whether or not sufficient time has passed since a previous instance of a survey being assigned to assign
     * a new one
     */
    public static boolean isRecurrenceWindowOpen(StudyEnvironmentSurvey studySurvey, ParticipantTask pastTask) {
        if (studySurvey.getSurvey().getRecurrenceType() == RecurrenceType.NONE) {
            return false;
        }
        Instant pastCutoffTime = ZonedDateTime.now(ZoneOffset.UTC)
                .minusDays(studySurvey.getSurvey().getRecurrenceIntervalDays()).toInstant();
        return pastTask.getCreatedAt().isBefore(pastCutoffTime);
    }

}
