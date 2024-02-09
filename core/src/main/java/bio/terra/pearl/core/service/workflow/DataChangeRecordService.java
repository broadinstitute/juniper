package bio.terra.pearl.core.service.workflow;

import bio.terra.pearl.core.dao.workflow.DataChangeRecordDao;
import bio.terra.pearl.core.model.audit.DataChangeRecord;
import bio.terra.pearl.core.service.ImmutableEntityService;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import java.util.List;
import java.util.UUID;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DataChangeRecordService extends ImmutableEntityService<DataChangeRecord, DataChangeRecordDao> {
    private EnrolleeService enrolleeService;


    public DataChangeRecordService(DataChangeRecordDao dao, @Lazy EnrolleeService enrolleeService) {
        super(dao);
        this.enrolleeService = enrolleeService;
    }

    public List<DataChangeRecord> findByEnrollee(UUID enrolleeId) {
        return dao.findByEnrolleeId(enrolleeId);
    }

    public List<DataChangeRecord> findByPortalEnvironmentId(UUID portalEnvId) {
        return dao.findByPortalEnvironmentId(portalEnvId);
    }

    public List<DataChangeRecord> findByModelId(UUID modelId) { return dao.findByModelId(modelId); }

    @Transactional
    public void deleteByPortalParticipantUserId(UUID ppUserId) {
        dao.deleteByPortalParticipantUserId(ppUserId);
    }

    @Transactional
    public void deleteByPortalEnvironmentId(UUID portalEnvId) {
        dao.deleteByPortalEnvironmentId(portalEnvId);
    }
    @Transactional
    public void deleteByEnrolleeId(UUID enrolleeId) {
        dao.deleteByEnrolleeId(enrolleeId);
    }
}
