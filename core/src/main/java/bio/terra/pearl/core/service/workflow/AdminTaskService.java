package bio.terra.pearl.core.service.workflow;

import bio.terra.pearl.core.dao.workflow.AdminTaskDao;
import bio.terra.pearl.core.dao.workflow.ParticipantTaskDao;
import bio.terra.pearl.core.model.workflow.AdminTask;
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
public class AdminTaskService extends CrudService<AdminTask, AdminTaskDao> {
    private static final Logger logger = LoggerFactory.getLogger(AdminUserService.class);

    public AdminTaskService(AdminTaskDao dao) {
        super(dao);
    }

    public List<AdminTask> findByEnrolleeId(UUID enrolleeId) {
        return dao.findByEnrolleeId(enrolleeId);
    }

    @Transactional
    public void deleteByEnrolleId(UUID enrolleeId) { dao.deleteByEnrolleeId(enrolleeId); }
    @Transactional
    public void deleteByStudyEnvironmentId(UUID studyEnvId) { dao.deleteByStudyEnvironmentId(studyEnvId);}

    public List<AdminTask> findByAssignee(UUID adminUserId) {
        return dao.findByAssignee(adminUserId);
    }

    @Transactional
    @Override
    public AdminTask update(AdminTask task) {
        if (task.getStatus().isTerminalStatus() && task.getCompletedAt() == null) {
            task.setCompletedAt(Instant.now());
        }
        return dao.update(task);
    }
}
