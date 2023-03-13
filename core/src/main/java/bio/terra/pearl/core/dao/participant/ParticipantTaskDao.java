package bio.terra.pearl.core.dao.participant;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class ParticipantTaskDao extends BaseMutableJdbiDao<ParticipantTask> {
    public ParticipantTaskDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<ParticipantTask> getClazz() {
        return ParticipantTask.class;
    }

    public List<ParticipantTask> findByEnrolleeId(UUID enrolleeId) {
        return findAllByProperty("enrollee_id", enrolleeId);
    }

    public Optional<ParticipantTask> findByPortalParticipantUserId(UUID taskId, UUID ppUserId) {
        return findByTwoProperties("id", taskId, "portal_participant_user_id", ppUserId);
    }

    public void deleteByEnrolleeId(UUID enrolleeId) {
        deleteByProperty("enrollee_id", enrolleeId);
    }

    public ParticipantTask updateTaskStatus(UUID taskId, TaskStatus newStatus) {
        ParticipantTask task = find(taskId).get();
        task.setStatus(newStatus);
        if (newStatus.isTerminalStatus()) {
            task.setCompletedAt(Instant.now());
        }
        return update(task);
    }



    public List<ParticipantTask> findByStatusAndTime(UUID studyEnvironmentId,
                                                     Duration timeSinceCreation,
                                                     Duration timeSinceLastNotification,
                                                     List<TaskStatus> statuses) {
        Instant taskCreationCutoff = Instant.now().minus(timeSinceCreation);
        Instant lastNotificationCutoff = Instant.now().minus(timeSinceLastNotification);
        return jdbi.withHandle(handle ->
                handle.createQuery("with enrollee_times as (select enrollee_id as notification_enrollee_id, MAX(created_at) as last_notification_time "
                        + " from notification where study_environment_id = :studyEnvironmentId group by enrollee_id) "
                        + " select * from " + tableName + " left join enrollee_times on enrollee_id = notification_enrollee_id "
                        + " where study_environment_id = :studyEnvironmentId and created_at < :taskCreationCutoff"
                        + " and created_at < :taskCreationCutoff and status in (<statuses>)"
                        + " and (:lastNotificationCutoff > last_notification_time OR last_notification_time IS NULL);")
                        .bind("studyEnvironmentId", studyEnvironmentId)
                        .bindList("statuses", statuses)
                        .bind("lastNotificationCutoff", lastNotificationCutoff)
                        .bind("taskCreationCutoff", taskCreationCutoff)
                        .mapTo(clazz)
                        .list()
        );
    }

}
