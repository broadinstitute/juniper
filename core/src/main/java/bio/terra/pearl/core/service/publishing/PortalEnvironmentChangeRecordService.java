package bio.terra.pearl.core.service.publishing;

import bio.terra.pearl.core.dao.publishing.PortalEnvironmentChangeRecordDao;
import bio.terra.pearl.core.model.publishing.PortalEnvironmentChangeRecord;
import bio.terra.pearl.core.service.ImmutableEntityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/** crud service for PortalEnvironmentChangeRecord -- does not handle actual publishing logic */
@Service
public class PortalEnvironmentChangeRecordService extends ImmutableEntityService<PortalEnvironmentChangeRecord, PortalEnvironmentChangeRecordDao> {
    public PortalEnvironmentChangeRecordService(PortalEnvironmentChangeRecordDao dao) {
        super(dao);
    }

    public List<PortalEnvironmentChangeRecord> findByPortalId(UUID portalId) {
        return dao.findByPortalId(portalId);
    }

    @Transactional
    public void deleteByPortalId(UUID portalId) {
        dao.deleteByPortalId(portalId);
    }
}
