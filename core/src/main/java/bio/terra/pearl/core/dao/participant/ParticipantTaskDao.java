package bio.terra.pearl.core.dao.participant;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskStatus;
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
}
