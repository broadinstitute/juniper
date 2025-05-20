package bio.terra.pearl.core.service.notification;

import bio.terra.pearl.core.dao.notification.NotificationDao;
import bio.terra.pearl.core.dao.workflow.ParticipantTaskDao;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import bio.terra.pearl.core.model.workflow.TaskType;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
public class ParticipantTaskQueryService {
    private ParticipantTaskDao participantTaskDao;
    private NotificationDao notificationDao;

    public ParticipantTaskQueryService(ParticipantTaskDao participantTaskDao, NotificationDao notificationDao) {
        this.participantTaskDao = participantTaskDao;
        this.notificationDao = notificationDao;
    }

    public List<ParticipantTaskDao.EnrolleeWithTasks> findByStatusAndTime(UUID studyEnvironmentId,
                                                                          TaskType taskType,
                                                                          Duration timeSinceCreation,
                                                                          Duration maxTimeSinceCreation,
                                                                          Duration timeSinceLastNotification,
                                                                          List<TaskStatus> statuses,
                                                                          List<String> targetStableIds,
                                                                          UUID triggerScopeId) {
        return participantTaskDao.findByStatusAndTime(studyEnvironmentId, taskType, timeSinceCreation, maxTimeSinceCreation,
                timeSinceLastNotification, statuses, targetStableIds, triggerScopeId);
    }

    public List<ParticipantTaskDao.EnrolleeWithTasks> findIncompleteByTime(UUID studyEnvironmentId,
                                                                           TaskType taskType,
                                                                           Duration timeSinceCreation,
                                                                           Duration maxTimeSinceCreation,
                                                                           Duration timeSinceLastNotification,
                                                                           List<String> targetStableIds,
                                                                           UUID triggerScopeId) {
        return findByStatusAndTime(studyEnvironmentId, taskType, timeSinceCreation, maxTimeSinceCreation, timeSinceLastNotification,
                List.of(TaskStatus.NEW, TaskStatus.IN_PROGRESS), targetStableIds, triggerScopeId);
    }
}
