package bio.terra.pearl.core.dao.workflow;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import bio.terra.pearl.core.model.workflow.TaskType;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
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

    public Map<UUID, List<ParticipantTask>> findByEnrolleeIds(Collection<UUID> enrolleeIds) {
        return findAllByPropertyCollection("enrollee_id", enrolleeIds)
                .stream().collect(Collectors.groupingBy(ParticipantTask::getEnrolleeId, Collectors.toList()));
    }

    /** Attempts to find a task for the given activity and study.  If there are multiple, it will return the first */
    public Optional<ParticipantTask> findTaskForActivity(UUID ppUserId, UUID studyEnvironmentId, String activityStableId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("select * from " + tableName + " where portal_participant_user_id = :ppUserId "
                                + " and target_stable_id = :activityStableId and study_environment_id = :studyEnvironmentId"
                        )
                        .bind("ppUserId", ppUserId)
                        .bind("activityStableId", activityStableId)
                        .bind("studyEnvironmentId", studyEnvironmentId)
                        .mapTo(clazz)
                        .stream().findFirst()
        );
    }

    public Optional<ParticipantTask> findByPortalParticipantUserId(UUID taskId, UUID ppUserId) {
        return findByTwoProperties("id", taskId, "portal_participant_user_id", ppUserId);
    }

    public void deleteByEnrolleeId(UUID enrolleeId) {
        deleteByProperty("enrollee_id", enrolleeId);
    }

    public List<EnrolleeWithTasks> findByStatusAndTime(UUID studyEnvironmentId,
                                                       TaskType taskType,
                                                       Duration minTimeSinceCreation,
                                                       Duration maxTimeSinceCreation,
                                                       Duration minTimeSinceLastNotification,
                                                       List<TaskStatus> statuses) {
        Instant minTimeSinceCreationInstant = Instant.now().minus(minTimeSinceCreation);
        Instant maxTimeSinceCreationInstant = Instant.now().minus(maxTimeSinceCreation);
        Instant lastNotificationCutoff = Instant.now().minus(minTimeSinceLastNotification);
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                        with enrollee_times as (select enrollee_id as notification_enrollee_id, MAX(created_at) as last_notification_time
                          from notification where study_environment_id = :studyEnvironmentId and delivery_status = 'SENT' group by enrollee_id)
                        select enrollee_id as enrolleeId, array_agg(target_name) as taskTargetNames, array_agg(id) as taskIds 
                        from participant_task
                        left join enrollee_times on enrollee_id = notification_enrollee_id
                        where study_environment_id = :studyEnvironmentId 
                        and task_type = :taskType
                        and created_at < :minTimeSinceCreationInstant 
                        and created_at > :maxTimeSinceCreationInstant 
                        and status in (<statuses>)
                        and (:lastNotificationCutoff > last_notification_time OR last_notification_time IS NULL)
                        group by enrollee_id order by enrollee_id;
                        """)
                        .bind("studyEnvironmentId", studyEnvironmentId)
                        .bindList("statuses", statuses)
                        .bind("lastNotificationCutoff", lastNotificationCutoff)
                        .bind("minTimeSinceCreationInstant", minTimeSinceCreationInstant)
                        .bind("maxTimeSinceCreationInstant", maxTimeSinceCreationInstant)
                        .bind("taskType", taskType)
                        .map(enrolleeWithTasksMapper)
                        .list()
        );
    }

    public List<EnrolleeTasks> findTasksByStudy(UUID studyEnvironmentId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("select distinct target_stable_id, target_name from " + tableName +
                                " where study_environment_id = :studyEnvironmentId")
                        .bind("studyEnvironmentId", studyEnvironmentId)
                        .map(enrolleeTasksMapper)
                        .list()
        );
    }

    public Optional<ParticipantTask> findByKitRequestId(UUID kitRequestId) {
        return findByProperty("kit_request_id", kitRequestId);
    }

    @Getter
    @Setter @NoArgsConstructor
    public static class EnrolleeWithTasks {
        private UUID enrolleeId;
        private List<String> taskTargetNames;
        private List<UUID> taskIds;
    }

    public final RowMapper<EnrolleeWithTasks> enrolleeWithTasksMapper = BeanMapper.of(EnrolleeWithTasks.class);

    @Getter
    @Setter @NoArgsConstructor
    @SuperBuilder
    public static class EnrolleeTasks {
        private String targetName;
        private String targetStableId;
    }

    public final RowMapper<EnrolleeTasks> enrolleeTasksMapper = BeanMapper.of(EnrolleeTasks.class);
}
