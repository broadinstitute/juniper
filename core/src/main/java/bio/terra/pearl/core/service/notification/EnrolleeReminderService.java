package bio.terra.pearl.core.service.notification;

import bio.terra.pearl.core.model.notification.TriggeredAction;
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
    private TriggeredActionService triggeredActionService;
    private EnrolleeRuleService enrolleeRuleService;
    private NotificationDispatcher notificationDispatcher;

    public EnrolleeReminderService(ParticipantTaskQueryService participantTaskQueryService,
                                   StudyEnvironmentService studyEnvironmentService,
                                   TriggeredActionService triggeredActionService,
                                   EnrolleeRuleService enrolleeRuleService,
                                   NotificationDispatcher notificationDispatcher) {
        this.participantTaskQueryService = participantTaskQueryService;
        this.studyEnvironmentService = studyEnvironmentService;
        this.triggeredActionService = triggeredActionService;
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
        var allEnvConfigs = triggeredActionService.findByStudyEnvironmentId(studyEnv.getId(), true);
        var reminderConfigs = allEnvConfigs.stream().filter(config ->
                config.getTriggerType().equals(TriggerType.TASK_REMINDER)).toList();
        for (var reminderConfig : reminderConfigs) {
            sendTaskReminders(studyEnv, reminderConfig);
        }
    }

    public void sendTaskReminders(StudyEnvironment studyEnv, TriggeredAction triggeredAction) {
        Duration timeSinceCreation = Duration.ofMinutes(triggeredAction.getAfterMinutesIncomplete());

        Duration timeSinceLastNotification = Duration.ofMinutes(triggeredAction.getReminderIntervalMinutes());
        long maxReminders = triggeredAction.getMaxNumReminders() <= 0 ? 100000 : triggeredAction.getMaxNumReminders();
        Duration maxTimeSinceCreation = timeSinceCreation.plus(timeSinceLastNotification.multipliedBy(maxReminders));
        var enrolleesWithTasks = participantTaskQueryService
                .findIncompleteByTime(studyEnv.getId(),
                        triggeredAction.getTaskType(),
                        timeSinceCreation,
                        maxTimeSinceCreation,
                        timeSinceLastNotification);
        log.info("Found {} enrollees with tasks needing reminder from config {}: taskType {}",
                enrolleesWithTasks.size(), triggeredAction.getId(), triggeredAction.getTaskType());

        // bulk load the enrollees
        List<EnrolleeRuleData> enrolleeData = enrolleeRuleService
                .fetchData(enrolleesWithTasks.stream().map(ewt -> ewt.getEnrolleeId()).toList());

        var envContext = notificationDispatcher.loadContextInfo(triggeredAction);

        for (var enrolleeWithTask : enrolleesWithTasks) {
            // this isn't an optimized match -- we're assuming the number of reminders we send on any given run for a single
            // config will likely be < 100
            EnrolleeRuleData ruleData = enrolleeData.stream()
                    .filter(erd -> erd.enrollee().getId().equals(enrolleeWithTask.getEnrolleeId())).findFirst().get();
            // don't send non-consent task reminders to enrollees who haven't consented
            if (triggeredAction.getTaskType().equals(TaskType.CONSENT) || ruleData.enrollee().isConsented()) {
                notificationDispatcher.dispatchNotification(triggeredAction, ruleData, envContext);
            }
        }
    }
}
