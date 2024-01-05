package bio.terra.pearl.core.service.notification;

import bio.terra.pearl.core.model.notification.Trigger;
import bio.terra.pearl.core.model.notification.TriggerType;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.workflow.TaskType;
import bio.terra.pearl.core.service.rule.EnrolleeRuleData;
import bio.terra.pearl.core.service.rule.EnrolleeRuleService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import java.time.Duration;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EnrolleeReminderService {
    private ParticipantTaskQueryService participantTaskQueryService;
    private StudyEnvironmentService studyEnvironmentService;
    private TriggerService triggerService;
    private EnrolleeRuleService enrolleeRuleService;
    private NotificationDispatcher notificationDispatcher;

    public EnrolleeReminderService(ParticipantTaskQueryService participantTaskQueryService,
                                   StudyEnvironmentService studyEnvironmentService,
                                   TriggerService triggerService,
                                   EnrolleeRuleService enrolleeRuleService,
                                   NotificationDispatcher notificationDispatcher) {
        this.participantTaskQueryService = participantTaskQueryService;
        this.studyEnvironmentService = studyEnvironmentService;
        this.triggerService = triggerService;
        this.enrolleeRuleService = enrolleeRuleService;
        this.notificationDispatcher = notificationDispatcher;
    }

    public void sendTaskReminders() {
        List<StudyEnvironment> studyEnvironments = studyEnvironmentService.findAll();
        for (StudyEnvironment studyEnv : studyEnvironments) {
            sendTaskReminders(studyEnv);
        }
    }

    public void sendTaskReminders(StudyEnvironment studyEnv) {
        log.info("querying enrollee reminder queries for study environment {} ({})", studyEnv.getId(), studyEnv.getEnvironmentName());
        var allEnvConfigs = triggerService.findByStudyEnvironmentId(studyEnv.getId(), true);
        var reminderConfigs = allEnvConfigs.stream().filter(config ->
                config.getTriggerType().equals(TriggerType.TASK_REMINDER)).toList();
        for (var reminderConfig : reminderConfigs) {
            sendTaskReminders(studyEnv, reminderConfig);
        }
    }

    public void sendTaskReminders(StudyEnvironment studyEnv, Trigger trigger) {
        Duration timeSinceCreation = Duration.ofMinutes(trigger.getAfterMinutesIncomplete());

        Duration timeSinceLastNotification = Duration.ofMinutes(trigger.getReminderIntervalMinutes());
        long maxReminders = trigger.getMaxNumReminders() <= 0 ? 100000 : trigger.getMaxNumReminders();
        Duration maxTimeSinceCreation = timeSinceCreation.plus(timeSinceLastNotification.multipliedBy(maxReminders));
        var enrolleesWithTasks = participantTaskQueryService
                .findIncompleteByTime(studyEnv.getId(),
                        trigger.getTaskType(),
                        timeSinceCreation,
                        maxTimeSinceCreation,
                        timeSinceLastNotification);
        log.info("Found {} enrollees with tasks needing reminder from config {}: taskType {}",
                enrolleesWithTasks.size(), trigger.getId(), trigger.getTaskType());

        // bulk load the enrollees
        List<EnrolleeRuleData> enrolleeData = enrolleeRuleService
                .fetchData(enrolleesWithTasks.stream().map(ewt -> ewt.getEnrolleeId()).toList());

        var envContext = notificationDispatcher.loadContextInfo(trigger);

        for (var enrolleeWithTask : enrolleesWithTasks) {
            // this isn't an optimized match -- we're assuming the number of reminders we send on any given run for a single
            // config will likely be < 100
            EnrolleeRuleData ruleData = enrolleeData.stream()
                    .filter(erd -> erd.enrollee().getId().equals(enrolleeWithTask.getEnrolleeId())).findFirst().get();
            // don't send non-consent task reminders to enrollees who haven't consented
            if (trigger.getTaskType().equals(TaskType.CONSENT) || ruleData.enrollee().isConsented()) {
                notificationDispatcher.dispatchNotification(trigger, ruleData, envContext);
            }
        }
    }
}
