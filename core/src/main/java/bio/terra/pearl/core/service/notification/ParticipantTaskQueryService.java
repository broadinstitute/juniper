package bio.terra.pearl.core.service.notification;

import bio.terra.pearl.core.dao.notification.NotificationDao;
import bio.terra.pearl.core.dao.participant.ParticipantTaskDao;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ParticipantTaskQueryService {
    private ParticipantTaskDao participantTaskDao;
    private NotificationDao notificationDao;

    public ParticipantTaskQueryService(ParticipantTaskDao participantTaskDao, NotificationDao notificationDao) {
        this.participantTaskDao = participantTaskDao;
        this.notificationDao = notificationDao;
    }

    public List<ParticipantTask> findByStatusAndTime(UUID studyEnvironmentId,
                                                 Duration timeSinceCreation,
                                                 Duration timeSinceLastNotification,
                                                 List<TaskStatus> statuses) {
        return participantTaskDao.findByStatusAndTime(studyEnvironmentId, timeSinceCreation,
                timeSinceLastNotification, statuses);
    }

    public List<ParticipantTask> findIncompleteByTime(UUID studyEnvironmentId,
                                                 Duration timeSinceCreation,
                                                 Duration timeSinceLastNotification) {
        return findByStatusAndTime(studyEnvironmentId, timeSinceCreation, timeSinceLastNotification,
                List.of(TaskStatus.NEW, TaskStatus.IN_PROGRESS));
    }
}
