package bio.terra.pearl.core.dao.participant;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import bio.terra.pearl.core.model.workflow.TaskType;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
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



    public List<EnrolleeWithTasks> findByStatusAndTime(UUID studyEnvironmentId,
                                                       TaskType taskType,
                                                       Duration minTimeSinceCreation,
                                                       Duration maxTimeSinceCreation,
                                                       Duration minTimeSinceLastNotification,
                                                       List<TaskStatus> statuses) {
        Instant taskCreationCutoff = Instant.now().minus(minTimeSinceCreation);
        Instant maxTaskCreationCutoff = Instant.now().minus(maxTimeSinceCreation);
        Instant lastNotificationCutoff = Instant.now().minus(minTimeSinceLastNotification);
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                        with enrollee_times as (select enrollee_id as notification_enrollee_id, MAX(created_at) as last_notification_time
                          from notification where study_environment_id = :studyEnvironmentId group by enrollee_id)
                        select enrollee_id as enrolleeId, array_agg(target_name) as taskTargetNames, array_agg(id) as taskIds 
                        from participant_task
                        left join enrollee_times on enrollee_id = notification_enrollee_id
                        where study_environment_id = :studyEnvironmentId 
                        and created_at < :taskCreationCutoff
                        and task_type = :taskType
                        and created_at < :taskCreationCutoff 
                        and created_at > :maxTaskCreationCutoff 
                        and status in (<statuses>)
                        and (:lastNotificationCutoff > last_notification_time OR last_notification_time IS NULL)
                        group by enrollee_id order by enrollee_id;
                        """)
                        .bind("studyEnvironmentId", studyEnvironmentId)
                        .bindList("statuses", statuses)
                        .bind("lastNotificationCutoff", lastNotificationCutoff)
                        .bind("taskCreationCutoff", taskCreationCutoff)
                        .bind("maxTaskCreationCutoff", maxTaskCreationCutoff)
                        .bind("taskType", taskType)
                        .map(enrolleeTasksMapper)
                        .list()
        );
    }

    @Getter
    @Setter @NoArgsConstructor
    public static class EnrolleeWithTasks {
        private UUID enrolleeId;
        private List<String> taskTargetNames;
        private List<UUID> taskIds;
    }

    public RowMapper enrolleeTasksMapper = BeanMapper.of(EnrolleeWithTasks.class);
}
