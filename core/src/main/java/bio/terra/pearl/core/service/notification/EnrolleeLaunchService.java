package bio.terra.pearl.core.service.notification;

import bio.terra.pearl.core.dao.workflow.ParticipantTaskDao;
import bio.terra.pearl.core.model.notification.Trigger;
import bio.terra.pearl.core.model.notification.TriggerType;
import bio.terra.pearl.core.model.participant.EnrolleeSourceType;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.service.rule.EnrolleeContext;
import bio.terra.pearl.core.service.rule.EnrolleeContextService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.workflow.ParticipantTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class EnrolleeLaunchService {
    private final ParticipantTaskQueryService participantTaskQueryService;
    private final StudyEnvironmentService studyEnvironmentService;
    private final TriggerService triggerService;
    private final EnrolleeContextService enrolleeContextService;
    private final NotificationDispatcher notificationDispatcher;
    private final ParticipantTaskService participantTaskService;
    private final NotificationService notificationService;

    public EnrolleeLaunchService(ParticipantTaskQueryService participantTaskQueryService,
                                 StudyEnvironmentService studyEnvironmentService,
                                 TriggerService triggerService,
                                 EnrolleeContextService enrolleeContextService,
                                 NotificationDispatcher notificationDispatcher, ParticipantTaskService participantTaskService, NotificationService notificationService) {
        this.participantTaskQueryService = participantTaskQueryService;
        this.studyEnvironmentService = studyEnvironmentService;
        this.triggerService = triggerService;
        this.enrolleeContextService = enrolleeContextService;
        this.notificationDispatcher = notificationDispatcher;
        this.participantTaskService = participantTaskService;
        this.notificationService = notificationService;
    }

    public void sendTaskLaunchEmails() {
        List<StudyEnvironment> studyEnvironments = studyEnvironmentService.findAll();
        for (StudyEnvironment studyEnv : studyEnvironments) {
            sendTaskLaunchEmails(studyEnv);
        }
    }

    public void sendTaskLaunchEmails(StudyEnvironment studyEnv) {
        log.info("querying enrollee survey launch triggers for study environment {} ({})", studyEnv.getId(), studyEnv.getEnvironmentName());
        List<Trigger> allEnvConfigs = triggerService.findByStudyEnvironmentId(studyEnv.getId(), true);
        List<Trigger> launchConfigs = allEnvConfigs.stream().filter(config ->
                config.getTriggerType().equals(TriggerType.LAUNCH)).toList();
        for (Trigger launchConfig : launchConfigs) {

            if (Objects.isNull(launchConfig.getFilterTargetStableIds()) || launchConfig.getFilterTargetStableIds().isEmpty()) {
                log.warn("No filter target stable IDs found for launch config {}", launchConfig.getId());
                continue;
            }

            launchConfig.getFilterTargetStableIds().forEach(stableId -> {
                if (hasTaskRecentlyLaunched(stableId, launchConfig)) {
                    sendLaunchEmails(studyEnv, launchConfig, stableId);
                }
            });
        }
    }

    public void sendLaunchEmails(StudyEnvironment studyEnv, Trigger trigger, String stableId) {
        Duration timeSinceLaunch = Duration.ofMinutes(trigger.getReminderIntervalMinutes());

        List<ParticipantTaskDao.EnrolleeWithTasks> enrolleesWithTasks = participantTaskQueryService
                .findRecentlyAssigned(studyEnv.getId(),
                        trigger.getTaskType(),
                        timeSinceLaunch,
                        List.of(stableId));

        log.info("Found {} enrollees with tasks needing launch email from config {}: taskType {}",
                enrolleesWithTasks.size(), trigger.getId(), trigger.getTaskType());

        // bulk load the enrollees
        List<EnrolleeContext> enrolleeData = enrolleeContextService
                .fetchData(enrolleesWithTasks.stream().map(ewt -> ewt.getEnrolleeId()).toList());

        NotificationContextInfo envContext = notificationDispatcher.loadContextInfo(trigger);

        for (EnrolleeContext enrolleeContext : enrolleeData) {
            // don't send non-consent task reminders to enrollees who haven't consented
            if (shouldSendReminder(enrolleeContext, trigger) && !hasBeenSentLaunchEmail(enrolleeContext, trigger)) {
                notificationDispatcher.dispatchNotification(trigger, enrolleeContext, envContext);
            }
        }
    }

    public boolean hasBeenSentLaunchEmail(EnrolleeContext enrolleeContext, Trigger trigger) {
        return !notificationService
                .findByEnrolleeAndTriggerId(enrolleeContext.getEnrollee().getId(), trigger.getId())
                .isEmpty();
    }

    public boolean shouldSendReminder(EnrolleeContext enrolleeContext, Trigger trigger) {
        // don't send reminders other than consents for enrollees who haven't consented
        if (!enrolleeContext.getEnrollee().isConsented()) {
            return false;
        }
        // don't send reminders to enrollees who were imported but haven't consented -- we want them to use invitation emails
        if (enrolleeContext.getEnrollee().getSource().equals(EnrolleeSourceType.IMPORT) &&
                 enrolleeContext.getParticipantUser().getLastLogin() == null) {
            return false;
        }
        return true;
    }

    public boolean hasTaskRecentlyLaunched(String stableId, Trigger trigger) {
        Optional<ParticipantTask> oldestTaskOpt = participantTaskService.findOldestTaskForActivity(trigger.getStudyEnvironmentId(), stableId);

        if (oldestTaskOpt.isEmpty()) {
            System.out.println("No task found for activity " + stableId);
            return false;
        }

        ParticipantTask oldestTask = oldestTaskOpt.get();

        return Instant
                .now()
                .minus(Duration.ofMinutes(trigger.getReminderIntervalMinutes()))
                .isBefore(oldestTask.getCreatedAt());

    }
}
