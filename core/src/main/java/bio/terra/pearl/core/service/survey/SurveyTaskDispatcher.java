package bio.terra.pearl.core.service.survey;

import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.audit.ResponsibleEntity;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.survey.SurveyType;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import bio.terra.pearl.core.model.workflow.TaskType;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import bio.terra.pearl.core.service.rule.EnrolleeRuleService;
import bio.terra.pearl.core.service.survey.event.SurveyPublishedEvent;
import bio.terra.pearl.core.service.workflow.*;
import bio.terra.pearl.core.service.rule.EnrolleeRuleData;
import bio.terra.pearl.core.service.rule.RuleEvaluator;
import bio.terra.pearl.core.service.study.StudyEnvironmentSurveyService;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

/** listens for events and updates enrollee survey tasks accordingly */
@Service
@Slf4j
public class SurveyTaskDispatcher {
    private StudyEnvironmentSurveyService studyEnvironmentSurveyService;
    private ParticipantTaskService participantTaskService;
    private EnrolleeService enrolleeService;
    private PortalParticipantUserService portalParticipantUserService;
    private EnrolleeRuleService enrolleeRuleService;


    public SurveyTaskDispatcher(StudyEnvironmentSurveyService studyEnvironmentSurveyService,
                                ParticipantTaskService participantTaskService,
                                EnrolleeService enrolleeService,
                                PortalParticipantUserService portalParticipantUserService,
                                EnrolleeRuleService enrolleeRuleService) {
        this.studyEnvironmentSurveyService = studyEnvironmentSurveyService;
        this.participantTaskService = participantTaskService;
        this.enrolleeService = enrolleeService;
        this.portalParticipantUserService = portalParticipantUserService;
        this.enrolleeRuleService = enrolleeRuleService;
    }


    public List<ParticipantTask> assign(ParticipantTaskAssignDto assignDto,
                                        UUID studyEnvironmentId,
                                        ResponsibleEntity responsibleEntity) {
        List<Enrollee> enrollees = findMatchingEnrollees(assignDto, studyEnvironmentId);
        StudyEnvironmentSurvey studyEnvironmentSurvey = studyEnvironmentSurveyService
                .findAllWithSurveyNoContent(List.of(studyEnvironmentId), assignDto.targetStableId(), true)
                        .stream().findFirst().orElseThrow(() -> new NotFoundException("Could not find active survey to assign tasks"));
        List<UUID> profileIds = enrollees.stream().map(Enrollee::getProfileId).toList();
        List<PortalParticipantUser> ppUsers = portalParticipantUserService.findByProfileIds(profileIds);
        if (ppUsers.size() != enrollees.size()) {
            throw new IllegalStateException("Task dispatch failed: Portal participant user not matched to enrollee");
        }
        List<EnrolleeRuleData> enrolleeRuleDatas = enrolleeRuleService.fetchData(enrollees.stream().map(Enrollee::getId).toList());

        UUID auditOperationId = UUID.randomUUID();
        List<ParticipantTask> createdTasks = new ArrayList<>();
        for (int i = 0; i < enrollees.size(); i++) {
            Optional<ParticipantTask> taskOpt;
            if (assignDto.overrideEligibility()) {
                taskOpt = Optional.of(buildTask(enrollees.get(i), ppUsers.get(i),
                        studyEnvironmentSurvey, studyEnvironmentSurvey.getSurvey()));
            } else {
                List<ParticipantTask> existingTasks = participantTaskService.findByEnrolleeId(enrollees.get(i).getId());
                taskOpt = buildTaskIfApplicable(enrollees.get(i), existingTasks, ppUsers.get(i), enrolleeRuleDatas.get(i),
                        studyEnvironmentSurvey, studyEnvironmentSurvey.getSurvey());
            }
            if (taskOpt.isPresent()) {
                DataAuditInfo auditInfo = DataAuditInfo.builder()
                        .portalParticipantUserId(ppUsers.get(i).getId())
                        .operationId(auditOperationId)
                        .enrolleeId(enrollees.get(i).getId()).build();
                auditInfo.setResponsibleEntity(responsibleEntity);
                ParticipantTask task = participantTaskService.create(taskOpt.get(), auditInfo);
                log.info("Task creation: enrollee {}  -- task {}, target {}", enrollees.get(i).getShortcode(),
                        task.getTaskType(), task.getTargetStableId());
                createdTasks.add(task);
            }
        }
        return createdTasks;
    }

    protected List<Enrollee> findMatchingEnrollees(ParticipantTaskAssignDto assignDto,
                                                   UUID studyEnvironmentId) {
        if (assignDto.assignAllUnassigned()) {
            return enrolleeService.findUnassignedToTask(studyEnvironmentId,
                    assignDto.targetStableId(), null);
        } else {
            return enrolleeService.findAll(assignDto.enrolleeIds());
        }
    }

    /** survey tasks could be triggered by just about anything, sbut for now we just listen to EnrolleeCreation */
    @EventListener
    @Order(DispatcherOrder.SURVEY_TASK)
    public void createSurveyTasks(EnrolleeCreationEvent enrolleeEvent) {
        DataAuditInfo auditInfo = DataAuditInfo.builder()
                .systemProcess(getClass().getSimpleName() + ".createSurveyTasks")
                .portalParticipantUserId(enrolleeEvent.getPortalParticipantUser().getId())
                .enrolleeId(enrolleeEvent.getEnrollee().getId()).build();
        List<StudyEnvironmentSurvey> studyEnvSurveys = studyEnvironmentSurveyService
                .findAllByStudyEnvIdWithSurvey(enrolleeEvent.getEnrollee().getStudyEnvironmentId());

        for (StudyEnvironmentSurvey studyEnvSurvey: studyEnvSurveys) {
            if (studyEnvSurvey.getSurvey().isAssignToAllNewEnrollees()) {
                Optional<ParticipantTask> taskOpt = buildTaskIfApplicable(enrolleeEvent.getEnrollee(),
                        enrolleeEvent.getEnrollee().getParticipantTasks(),
                        enrolleeEvent.getPortalParticipantUser(), enrolleeEvent.getEnrolleeRuleData(),
                        studyEnvSurvey, studyEnvSurvey.getSurvey());
                if (taskOpt.isPresent()) {
                    ParticipantTask task = taskOpt.get();
                    log.info("Task creation: enrollee {}  -- task {}, target {}", enrolleeEvent.getEnrollee().getShortcode(),
                            task.getTaskType(), task.getTargetStableId());
                    task = participantTaskService.create(task, auditInfo);
                    enrolleeEvent.getEnrollee().getParticipantTasks().add(task);
                }
            }
        }
    }

    @EventListener
    @Order(DispatcherOrder.SURVEY_TASK)
    public void updateSurveyTaskVersions(SurveyPublishedEvent event) {
        if (event.getSurvey().isAutoUpdateTaskAssignments()) {
            ParticipantTaskAssignDto assignDto = new ParticipantTaskAssignDto(TaskType.SURVEY,
                    event.getSurvey().getStableId(),
                    event.getSurvey().getVersion(),
                    )

        }
    }


    /** builds any survey tasks that the enrollee is eligible for that are not duplicates
     *  Does not add them to the event or persist them.
     *  */
    public Optional<ParticipantTask> buildTaskIfApplicable(Enrollee enrollee,
                                                      List<ParticipantTask> existingEnrolleeTasks,
                                                      PortalParticipantUser portalParticipantUser,
                                                      EnrolleeRuleData enrolleeRuleData,
                                                      StudyEnvironmentSurvey studyEnvSurvey, Survey survey) {
        if (isEligibleForSurvey(survey.getEligibilityRule(), enrolleeRuleData)) {
            ParticipantTask task = buildTask(enrollee, portalParticipantUser, studyEnvSurvey, studyEnvSurvey.getSurvey());
            if (!isDuplicateTask(studyEnvSurvey, task, existingEnrolleeTasks)) {
                return Optional.of(task);
            }
        }
        return Optional.empty();
    }

    public static boolean isEligibleForSurvey(String eligibilityRule, EnrolleeRuleData enrolleeRuleData) {
        return RuleEvaluator.evaluateEnrolleeRule(eligibilityRule, enrolleeRuleData);
    }

    /** builds a task for the given survey -- does NOT evaluate the rule or check duplicates */
    public ParticipantTask buildTask(Enrollee enrollee, PortalParticipantUser portalParticipantUser,
                                     StudyEnvironmentSurvey studyEnvSurvey, Survey survey) {
        if (!studyEnvSurvey.getSurveyId().equals(survey.getId())) {
            throw new IllegalArgumentException("Survey does not match StudyEnvironmentSurvey");
        }
        TaskType taskType = SurveyType.OUTREACH.equals(survey.getSurveyType()) ?
                TaskType.OUTREACH : TaskType.SURVEY;
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

    /**
     * To avoid accidentally assigning the same survey or outreach activity to a participant multiple times,
     * confirm that if the stableId matches an existing task, the existing task must be complete and the
     * configured survey must allow recurrence.
     */
    public static boolean isDuplicateTask(StudyEnvironmentSurvey studySurvey, ParticipantTask task,
                                   List<ParticipantTask> allTasks) {
        return !allTasks.stream()
                .filter(existingTask ->
                        (existingTask.getTaskType() == TaskType.SURVEY || existingTask.getTaskType() == TaskType.OUTREACH) &&
                        existingTask.getTargetStableId().equals(task.getTargetStableId()) &&
                        !isRecurrenceWindowOpen(studySurvey, existingTask))
                .toList().isEmpty();
    }

    /**
     * whether or not sufficient time has passed since a previous instance of a survey being assigned to assign
     * a new one
     */
    public static boolean isRecurrenceWindowOpen(StudyEnvironmentSurvey studySurvey, ParticipantTask pastTask) {
        if (!studySurvey.getSurvey().isRecur()) {
            return false;
        }
        Instant pastCutoffTime = ZonedDateTime.now(ZoneOffset.UTC)
                .minusDays(studySurvey.getSurvey().getRecurrenceIntervalDays() - RECUR_TASK_BUFFER_DAYS).toInstant();
        return pastTask.getCreatedAt().isBefore(pastCutoffTime);
    }

    /**
     * this is a fudge factor in our logic to prevent duplicate survey assignments -- we don't want to
     * allow duplicate tasks assigned, but we don't want to prevent tasks from being assigned just because
     * of time zone fuzziness, leap years, etc...
     */
    public static final int RECUR_TASK_BUFFER_DAYS = 3;
}
