package bio.terra.pearl.core.service.participant;

import bio.terra.pearl.core.dao.participant.ParticipantTaskDao;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.service.CrudService;
import bio.terra.pearl.core.service.admin.AdminUserService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ParticipantTaskService extends CrudService<ParticipantTask, ParticipantTaskDao> {
    private static final Logger logger = LoggerFactory.getLogger(AdminUserService.class);

    public ParticipantTaskService(ParticipantTaskDao dao) {
        super(dao);
    }

    public List<ParticipantTask> findByEnrolleeId(UUID enrolleeId) {
        return dao.findByEnrolleeId(enrolleeId);
    }

    public void deleteByEnrolleeId(UUID enrolleeId) { dao.deleteByEnrolleeId(enrolleeId);}

    public Optional<ParticipantTask> authTaskToPortalParticipantUser(UUID taskId, UUID ppUserId) {
        return dao.findByPortalParticipantUserId(taskId, ppUserId);
    }

    @Transactional
    @Override
    public ParticipantTask create(ParticipantTask task) {
        ParticipantTask savedTask = dao.create(task);
        logger.info("ParticipantTask created - id: {}, targetStableId: {}, enrolleeId: {}",
                task.getId(), task.getTargetStableId(), task.getEnrolleeId());
        return savedTask;
    }

    @Transactional
    public ParticipantTask update(ParticipantTask task) {
        if (task.getStatus().isTerminalStatus() && task.getCompletedAt() == null) {
            task.setCompletedAt(Instant.now());
        }
        return dao.update(task);
    }
}
