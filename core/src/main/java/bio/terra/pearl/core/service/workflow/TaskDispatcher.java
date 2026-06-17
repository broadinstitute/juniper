package bio.terra.pearl.core.service.workflow;

import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.audit.ResponsibleEntity;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.workflow.*;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import bio.terra.pearl.core.service.rule.EnrolleeContext;
import bio.terra.pearl.core.service.rule.EnrolleeContextService;
import bio.terra.pearl.core.service.search.EnrolleeSearchContext;
import bio.terra.pearl.core.service.search.EnrolleeSearchExpressionParser;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Handles dispatching tasks to enrollees based on task configurations.
 *
 * This service can be thought of as the hard-coded counterpart to TriggerActionService.  While those classes handle
 * custom actions configured for a particiular study (e.g. when this survey is completed, send a particular email),
 * this class and subclasses handle hardcoded Juniper actions such as "when a participant enrolls, assign them all the eligible surveys
 */
@Service
@Slf4j
public abstract class TaskDispatcher<T extends TaskConfig> {
    private final StudyEnvironmentService studyEnvironmentService;
    private final ParticipantTaskService participantTaskService;
    private final EnrolleeService enrolleeService;
    private final EnrolleeSearchExpressionParser enrolleeSearchExpressionParser;
    private final EnrolleeContextService enrolleeContextService;
    private final PortalParticipantUserService portalParticipantUserService;


    public TaskDispatcher(StudyEnvironmentService studyEnvironmentService,
                          ParticipantTaskService participantTaskService,
                          EnrolleeService enrolleeService,
                          EnrolleeSearchExpressionParser enrolleeSearchExpressionParser,
                          EnrolleeContextService enrolleeContextService,
                          PortalParticipantUserService portalParticipantUserService) {
        this.studyEnvironmentService = studyEnvironmentService;
        this.participantTaskService = participantTaskService;
        this.enrolleeService = enrolleeService;
        this.enrolleeSearchExpressionParser = enrolleeSearchExpressionParser;
        this.enrolleeContextService = enrolleeContextService;
        this.portalParticipantUserService = portalParticipantUserService;
    }

    protected abstract List<T> findTaskConfigsByStudyEnvironment(UUID studyEnvId);

    protected abstract Optional<T> findTaskConfigForAssignDto(UUID studyEnvironmentId, ParticipantTaskAssignDto participantTaskAssignDto);

    protected abstract TaskType getTaskType(T taskDispatchConfig);

    public void assignScheduledTasks() {
        List<StudyEnvironment> studyEnvironments = studyEnvironmentService.findAll();
        for (StudyEnvironment studyEnv : studyEnvironments) {
            assignScheduledTasks(studyEnv);
        }
    }

    public List<ParticipantTask> assign(ParticipantTaskAssignDto assignDto,
                                        UUID studyEnvironmentId,
                                        ResponsibleEntity operator) {
        List<Enrollee> enrollees = findMatchingEnrollees(assignDto, studyEnvironmentId);
        Optional<T> taskConfigOpt = findTaskConfigForAssignDto(studyEnvironmentId, assignDto);
        if (taskConfigOpt.isEmpty()) {
            throw new IllegalArgumentException("Could not find task config: " + assignDto.targetStableId() + " version " + assignDto.targetAssignedVersion());
        }
        return assign(enrollees, taskConfigOpt.get(), assignDto.overrideEligibility(), assignDto.justification(), operator);
    }

    public List<ParticipantTask> assign(List<Enrollee> enrollees,
                                        T taskDispatchConfig,
                                        boolean overrideEligibility,
                                        String justification,
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
                        taskDispatchConfig));
            } else {
                taskOpt = buildTaskIfApplicable(enrollees.get(i), existingTasks, ppUsers.get(i), enrolleeRuleData.get(i),
                        taskDispatchConfig);
            }
            if (taskOpt.isPresent()) {
                ParticipantTask task = taskOpt.get();
                copyForwardDataIfApplicable(task, taskDispatchConfig, existingTasks);
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

    protected void syncTasksForEnrollee(EnrolleeContext enrolleeContext) {
        Enrollee enrollee = enrolleeContext.getEnrollee();
        PortalParticipantUser ppUser = portalParticipantUserService.findByProfileId(enrollee.getProfileId()).orElseThrow(() -> new IllegalStateException("Could not find portal participant user for enrollee"));

        DataAuditInfo auditInfo = DataAuditInfo.builder()
                .systemProcess(getClass().getSimpleName() + ".updateSurveyTasks")
                .portalParticipantUserId(ppUser.getId())
                .enrolleeId(enrollee.getId()).build();

        List<T> taskAssignables = findTaskConfigsByStudyEnvironment(enrollee.getStudyEnvironmentId());

        for (T taskDispatchConfig : taskAssignables) {
            if (taskDispatchConfig.isAutoAssign()) {
                createTaskIfApplicable(taskDispatchConfig, enrolleeContext, ppUser, auditInfo);
            }
        }
    }

    public void updateTasksForNewTaskConfig(T newTaskConfig) {
        if (newTaskConfig.isAutoUpdateTaskAssignments()) {
            ParticipantTaskUpdateDto updateDto = new ParticipantTaskUpdateDto(
                    List.of(new ParticipantTaskUpdateDto.TaskUpdateSpec(
                            newTaskConfig.getStableId(),
                            newTaskConfig.getVersion(),
                            null,
                            null)),
                    null,
                    true
            );
            participantTaskService.updateTasks(
                    newTaskConfig.getStudyEnvironmentId(),
                    updateDto,
                    new ResponsibleEntity(DataAuditInfo.systemProcessName(getClass(), "handleSurveyPublished.autoUpdateTaskAssignments"))
            );
        }
        if (newTaskConfig.isAssignToExistingEnrollees()) {
            ParticipantTaskAssignDto assignDto = ParticipantTaskAssignDto.builder()
                    .taskType(getTaskType(newTaskConfig))
                    .targetStableId(newTaskConfig.getStableId())
                    .targetAssignedVersion(newTaskConfig.getVersion())
                    .assignAllUnassigned(true)
                    .justification(newTaskConfig.getName() + " published")
                    .build();

            assign(assignDto, newTaskConfig.getStudyEnvironmentId(),
                    new ResponsibleEntity(DataAuditInfo.systemProcessName(getClass(), "handleNewAssignableTask.assignToExistingEnrollees")));

        }
    }


    private void assignScheduledTasks(StudyEnvironment studyEnv) {
        List<T> assignableTasks = findTaskConfigsByStudyEnvironment(studyEnv.getId());
        for (T assignableTask : assignableTasks) {
            if (assignableTask.getRecurrenceType() != RecurrenceType.NONE && assignableTask.getRecurrenceIntervalDays() != null) {
                assignRecurring(assignableTask);
            }
            if (assignableTask.getDaysAfterEligible() != null && assignableTask.getDaysAfterEligible() > 0) {
                assignDelayed(assignableTask);
            }
        }
    }

    /**
     * will assign a recurring task to enrollees who have already taken it at least once, but are due to take it again
     */
    private void assignRecurring(T taskConfig) {
        // not the most efficient, but lets us reuse "isRecurrenceWindowOpen"
        List<Enrollee> enrolleesWithTask = enrolleeService.findAssignedToTask(taskConfig.getStudyEnvironmentId(), taskConfig.getStableId());
        Map<UUID, ParticipantTask> latestTasks = participantTaskService.findLatestNotRemovedByEnrolleeIds(
                enrolleesWithTask.stream().map(Enrollee::getId).toList(),
                taskConfig.getStableId());
        
        List<Enrollee> eligibleEnrollees = new ArrayList<>();

        for (Enrollee enrollee : enrolleesWithTask) {
            ParticipantTask latestTask = latestTasks.get(enrollee.getId());
            if (latestTask != null
                    && isRecurrenceWindowOpen(taskConfig, latestTask)) {
                eligibleEnrollees.add(enrollee);
            }
        }

        assign(eligibleEnrollees, taskConfig, false, "scheduled",
                new ResponsibleEntity(DataAuditInfo.systemProcessName(getClass(), "assignRecurringSurvey")));
    }

    /**
     * will assign a delayed task to enrollees who have never taken it, but are due to take it now
     */
    private void assignDelayed(T taskConfig) {
        List<Enrollee> enrollees = enrolleeService.findUnassignedToTask(taskConfig.getStudyEnvironmentId(), taskConfig.getStableId(), null);
        enrollees = enrollees.stream().filter(enrollee ->
                enrollee.getCreatedAt().plus(taskConfig.getDaysAfterEligible(), ChronoUnit.DAYS)
                        .isBefore(Instant.now())).toList();
        assign(enrollees, taskConfig, false, "scheduled", new ResponsibleEntity(DataAuditInfo.systemProcessName(getClass(), "assignDelayedSurvey")));
    }

    private void createTaskIfApplicable(T taskDispatchConfig,
                                        EnrolleeContext enrolleeContext,
                                        PortalParticipantUser ppUser,
                                        DataAuditInfo auditInfo) {
        Optional<ParticipantTask> taskOpt = buildTaskIfApplicable(enrolleeContext.getEnrollee(),
                enrolleeContext.getEnrollee().getParticipantTasks(),
                ppUser,
                enrolleeContext,
                taskDispatchConfig);
        if (taskOpt.isPresent()) {
            ParticipantTask task = taskOpt.get();
            log.info("Task creation: enrollee {}  -- task {}, target {}", enrolleeContext.getEnrollee().getShortcode(),
                    task.getTaskType(), task.getTargetStableId());
            task = participantTaskService.create(task, auditInfo);
            enrolleeContext.getEnrollee().getParticipantTasks().add(task);
        }
    }

    /** builds any tasks that the enrollee is eligible for that are not duplicates
     *  Does not add them to the event or persist them.
     *  */
    private Optional<ParticipantTask> buildTaskIfApplicable(Enrollee enrollee,
                                                      List<ParticipantTask> existingEnrolleeTasks,
                                                      PortalParticipantUser portalParticipantUser,
                                                      EnrolleeContext enrolleeContext,
                                                           T taskDispatchConfig) {
        if (isEligible(taskDispatchConfig, enrolleeContext)) {
            ParticipantTask task = buildTask(enrollee, portalParticipantUser, taskDispatchConfig);
            if (!isDuplicateTask(taskDispatchConfig, task, existingEnrolleeTasks)) {
                return Optional.of(task);
            }
        }
        return Optional.empty();
    }

    /**
     * builds a task for the given survey -- does NOT evaluate the rule or check duplicates
     */
    public ParticipantTask buildTask(Enrollee enrollee,
                                     PortalParticipantUser portalParticipantUser,
                                     T taskConfig) {
        TaskType taskType = getTaskType(taskConfig);
        ParticipantTask task = ParticipantTask.builder()
                .enrolleeId(enrollee.getId())
                .portalParticipantUserId(portalParticipantUser.getId())
                .studyEnvironmentId(enrollee.getStudyEnvironmentId())
                .blocksHub(taskConfig.isRequired())
                .taskOrder(taskConfig.getTaskOrder())
                .targetStableId(taskConfig.getStableId())
                .targetAssignedVersion(taskConfig.getVersion())
                .taskType(taskType)
                .targetName(taskConfig.getName())
                .status(TaskStatus.NEW)
                .build();
        return task;
    }

    private void copyForwardDataIfApplicable(ParticipantTask task, T taskDispatchConfig, List<ParticipantTask> existingTasks) {
        if (taskDispatchConfig.getRecurrenceType().equals(RecurrenceType.UPDATE)) {
            Optional<ParticipantTask> existingTask = existingTasks.stream()
                    .filter(t -> t.getTargetStableId().equals(task.getTargetStableId()))
                    .filter(t -> t.getStatus() != TaskStatus.REMOVED && t.getStatus() != TaskStatus.REJECTED)
                    .max(Comparator.comparing(ParticipantTask::getCreatedAt));
            existingTask.ifPresent(participantTask -> copyTaskData(task, participantTask, taskDispatchConfig));
        }
        else if(taskDispatchConfig.getRecurrenceType().equals(RecurrenceType.LONGITUDINAL)) {
            Optional<ParticipantTask> existingTask = existingTasks.stream()
                    .filter(t -> t.getTargetStableId() != null)
                    .filter(t -> t.getTargetStableId().equals(task.getTargetStableId()))
                    .filter(t -> t.getStatus() != TaskStatus.REMOVED && t.getStatus() != TaskStatus.REJECTED)
                    .max(Comparator.comparing(ParticipantTask::getCreatedAt));
            existingTask.ifPresent(participantTask -> copyTaskData(task, participantTask, taskDispatchConfig));
        }
    }

    private boolean isEligible(T taskDispatchConfig, EnrolleeContext enrolleeContext) {
        /**
         * eligible if the enrollee is a subject, the task is not restricted by time, and the enrollee meets the rule
         * note that this does not include a duplicate task check -- that is done elsewhere
         */
        // TODO JN-977: this logic will need to change because we will need to support surveys for proxies
        return enrolleeContext.getEnrollee().isSubject() &&
                (taskDispatchConfig.getDaysAfterEligible() == null ||
                        enrolleeContext.getEnrollee().getCreatedAt().plus(taskDispatchConfig.getDaysAfterEligible(), ChronoUnit.DAYS).isBefore(Instant.now())) &&
                enrolleeSearchExpressionParser
                        .parseRule(taskDispatchConfig.getEligibilityRule())
                        .evaluate(new EnrolleeSearchContext(enrolleeContext.getEnrollee(), enrolleeContext.getProfile()));
    }


    private List<Enrollee> findMatchingEnrollees(ParticipantTaskAssignDto assignDto,
                                                   UUID studyEnvironmentId) {
        if (assignDto.assignAllUnassigned()
                && (Objects.isNull(assignDto.enrolleeIds()) || assignDto.enrolleeIds().isEmpty())
                && (Objects.isNull(assignDto.enrolleeShortcodes()) || assignDto.enrolleeShortcodes().isEmpty())) {
            return enrolleeService.findUnassignedToTask(studyEnvironmentId,
                    assignDto.targetStableId(), null);
        } else if (!Objects.isNull(assignDto.enrolleeIds()) && !assignDto.enrolleeIds().isEmpty()) {
            List<Enrollee> found = enrolleeService.findAll(assignDto.enrolleeIds()).stream().filter(enrollee -> enrollee.getStudyEnvironmentId().equals(studyEnvironmentId)).toList();

            if (found.size() != assignDto.enrolleeIds().size()) {
                Set<UUID> foundShortcodes = new HashSet<>(found.stream().map(Enrollee::getId).toList());
                List<UUID> missing = assignDto.enrolleeIds().stream().distinct()
                        .filter(shortcode -> !foundShortcodes.contains(shortcode)).toList();

                throw new IllegalArgumentException("Could not find enrollees: " + missing);
            }

            return found;
        } else {
            List<String> requestedShortcodes = assignDto.enrolleeShortcodes();
            List<Enrollee> found = enrolleeService.findAllByShortcodes(requestedShortcodes, studyEnvironmentId);
            if (found.size() != requestedShortcodes.stream().distinct().count()) {
                Set<String> foundShortcodes = new HashSet<>(found.stream().map(Enrollee::getShortcode).toList());
                List<String> missing = requestedShortcodes.stream().distinct()
                        .filter(shortcode -> !foundShortcodes.contains(shortcode)).toList();
                throw new IllegalArgumentException("Could not find enrollees: " + missing);
            }
            return found;
        }
    }

    /**
     * To avoid accidentally assigning the same tasks or outreach activity to a participant multiple times,
     * confirm that if the stableId matches an existing task, the existing task must be complete and the
     * configured survey must allow recurrence.
     */
    public boolean isDuplicateTask(T taskDispatchConfig, ParticipantTask task,
                                   List<ParticipantTask> allTasks) {
        // get all non-removed tasks matching this current task config
        List<ParticipantTask> matchingTasks = allTasks.stream()
                .filter(existingTask -> existingTask.getStatus() != TaskStatus.REMOVED && existingTask.getStatus() != TaskStatus.REJECTED)
                .filter(existingTask -> existingTask.getTargetStableId() != null && existingTask.getTargetStableId().equals(task.getTargetStableId()))
                .sorted(Comparator.comparing(ParticipantTask::getCreatedAt).reversed())
                .toList();

        if (matchingTasks.isEmpty()) {
            return false; // first task for this stableId, so it can't be a duplicate
        }


        // if there's an existing task and it's not recurring, it's a duplicate
        if (taskDispatchConfig.getRecurrenceType() == RecurrenceType.NONE) {
            return true; // no recurrence, so it's a duplicate
        }

        // if it's recurring, check against the latest task to see if recurrence window is open.
        // if not, then it's a duplicate, we already have the needed task.
        ParticipantTask latestTask = matchingTasks.get(0);
        return !isRecurrenceWindowOpen(taskDispatchConfig, latestTask);
    }

    /**
     * whether or not sufficient time has passed since a previous instance of a task being assigned to assign
     * a new one. must be used with the latest non-removed task
     */
    private boolean isRecurrenceWindowOpen(T taskDispatchConfig, ParticipantTask latestTask) {
        if (taskDispatchConfig.getRecurrenceType() == RecurrenceType.NONE) {
            return false;
        }

        Instant date = taskDispatchConfig.getRecurOn() == RecurOn.COMPLETION
                ? latestTask.getCompletedAt()
                : latestTask.getCreatedAt();

        if (date == null || (taskDispatchConfig.getRecurOn() == RecurOn.COMPLETION && latestTask.getStatus() != TaskStatus.COMPLETE)) {
            return false; // never recur on incomplete tasks
        }

        Instant pastCutoffTime = ZonedDateTime.now(ZoneOffset.UTC)
                .minusDays(taskDispatchConfig.getRecurrenceIntervalDays()).toInstant();
        return date.isBefore(pastCutoffTime);
    }

    protected void copyTaskData(ParticipantTask newTask, ParticipantTask oldTask, T taskDispatchConfig) {
        BeanUtils.copyProperties(
                oldTask,
                newTask,
                "id", "createdAt", "lastUpdatedAt", "completedAt",
                "version", "status", "taskType",
                "targetName", "targetStableId", "targetAssignedVersion",
                "taskOrder", "blocksHub", "studyEnvironmentId",
                "enrolleeId", "portalParticipantUserId");
    }
}
