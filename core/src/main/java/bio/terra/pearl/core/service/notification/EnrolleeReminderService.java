package bio.terra.pearl.core.service.notification;

import bio.terra.pearl.core.model.notification.NotificationConfig;
import bio.terra.pearl.core.model.notification.NotificationType;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.workflow.TaskType;
import bio.terra.pearl.core.service.rule.EnrolleeRuleData;
import bio.terra.pearl.core.service.rule.EnrolleeRuleService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import java.time.Duration;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EnrolleeReminderService {
    private ParticipantTaskQueryService participantTaskQueryService;
    private StudyEnvironmentService studyEnvironmentService;
    private NotificationConfigService notificationConfigService;
    private EnrolleeRuleService enrolleeRuleService;
    private NotificationDispatcher notificationDispatcher;

    private static final Logger logger = LoggerFactory.getLogger(EnrolleeReminderService.class);

    public EnrolleeReminderService(ParticipantTaskQueryService participantTaskQueryService,
                                   StudyEnvironmentService studyEnvironmentService,
                                   NotificationConfigService notificationConfigService,
                                   EnrolleeRuleService enrolleeRuleService,
                                   NotificationDispatcher notificationDispatcher) {
        this.participantTaskQueryService = participantTaskQueryService;
        this.studyEnvironmentService = studyEnvironmentService;
        this.notificationConfigService = notificationConfigService;
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
        logger.info("querying enrollee reminder queries for study environment {} ({})", studyEnv.getId(), studyEnv.getEnvironmentName());
        var allEnvConfigs = notificationConfigService.findByStudyEnvironmentId(studyEnv.getId(), true);
        var reminderConfigs = allEnvConfigs.stream().filter(config ->
                config.getNotificationType().equals(NotificationType.TASK_REMINDER)).toList();
        for (var reminderConfig : reminderConfigs) {
            sendTaskReminders(studyEnv, reminderConfig);
        }
    }

    public void sendTaskReminders(StudyEnvironment studyEnv, NotificationConfig notificationConfig) {
        Duration timeSinceCreation = Duration.ofMinutes(notificationConfig.getAfterMinutesIncomplete());

        Duration timeSinceLastNotification = Duration.ofMinutes(notificationConfig.getReminderIntervalMinutes());
        long maxReminders = notificationConfig.getMaxNumReminders() <= 0 ? 100000 : notificationConfig.getMaxNumReminders();
        Duration maxTimeSinceCreation = timeSinceCreation.plus(timeSinceLastNotification.multipliedBy(maxReminders));
        var enrolleesWithTasks = participantTaskQueryService
                .findIncompleteByTime(studyEnv.getId(),
                        notificationConfig.getTaskType(),
                        timeSinceCreation,
                        maxTimeSinceCreation,
                        timeSinceLastNotification);
        logger.info("Found {} enrollees with tasks needing reminder from config {}: taskType {}",
                enrolleesWithTasks.size(), notificationConfig.getId(), notificationConfig.getTaskType());

        // bulk load the enrollees
        List<EnrolleeRuleData> enrolleeData = enrolleeRuleService
                .fetchData(enrolleesWithTasks.stream().map(ewt -> ewt.getEnrolleeId()).toList());

        var envContext = notificationDispatcher.loadContextInfo(notificationConfig);

        for (var enrolleeWithTask : enrolleesWithTasks) {
            // this isn't an optimized match -- we're assuming the number of reminders we send on any given run for a single
            // config will likely be < 100
            EnrolleeRuleData ruleData = enrolleeData.stream()
                    .filter(erd -> erd.enrollee().getId().equals(enrolleeWithTask.getEnrolleeId())).findFirst().get();
            // don't send non-consent task reminders to enrollees who haven't consented
            if (notificationConfig.getTaskType().equals(TaskType.CONSENT) || ruleData.enrollee().isConsented()) {
                notificationDispatcher.dispatchNotification(notificationConfig, ruleData, envContext);
            }
        }
    }
}
