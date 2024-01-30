package bio.terra.pearl.core.service.workflow;

import bio.terra.pearl.core.dao.workflow.ParticipantTaskDao;
import bio.terra.pearl.core.model.workflow.DataAuditInfo;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.service.CrudService;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import bio.terra.pearl.core.service.DataAuditedService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class ParticipantTaskService extends DataAuditedService<ParticipantTask, ParticipantTaskDao> {
    public ParticipantTaskService(ParticipantTaskDao dao, DataChangeRecordService dataChangeRecordService, ObjectMapper objectMapper) {
        super(dao, dataChangeRecordService, objectMapper);
    }

    public List<ParticipantTask> findByEnrolleeId(UUID enrolleeId) {
        return dao.findByEnrolleeId(enrolleeId);
    }

    public Map<UUID, List<ParticipantTask>> findByEnrolleeIds(List<UUID> enrolleeIds) {
        return dao.findByEnrolleeIds(enrolleeIds);
    }

    public List<ParticipantTask> findTasksByStudyAndTarget(UUID studyEnvId, List<String> targetStableIds) {
        return dao.findTasksByStudyAndTarget(studyEnvId, targetStableIds);
    }

    public void deleteByEnrolleeId(UUID enrolleeId) { dao.deleteByEnrolleeId(enrolleeId);}

    public Optional<ParticipantTask> authTaskToPortalParticipantUser(UUID taskId, UUID ppUserId) {
        return dao.findByPortalParticipantUserId(taskId, ppUserId);
    }

    public Optional<ParticipantTask> findTaskForActivity(UUID ppUserId, UUID studyEnvironmentId, String activityStableId) {
        return dao.findTaskForActivity(ppUserId, studyEnvironmentId, activityStableId);
    }

    public Optional<ParticipantTask> findByKitRequestId(UUID kitRequestId) {
        return dao.findByKitRequestId(kitRequestId);
    }

    @Transactional
    @Override
    public ParticipantTask update(ParticipantTask task, DataAuditInfo dataAuditInfo) {
        if (task.getStatus().isTerminalStatus() && task.getCompletedAt() == null) {
            task.setCompletedAt(Instant.now());
        }
        return super.update(task, dataAuditInfo);
    }
}
