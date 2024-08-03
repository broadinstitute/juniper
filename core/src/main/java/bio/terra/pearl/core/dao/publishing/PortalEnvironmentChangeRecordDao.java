package bio.terra.pearl.core.dao.publishing;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.publishing.PortalEnvironmentChangeRecord;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class PortalEnvironmentChangeRecordDao extends BaseJdbiDao<PortalEnvironmentChangeRecord> {
    public PortalEnvironmentChangeRecordDao(Jdbi jdbi) {
        super(jdbi);
    }

    public List<PortalEnvironmentChangeRecord> findByPortalId(UUID portalId) {
        return findAllByProperty("portal_id", portalId);
    }

    public void deleteByPortalId(UUID portalId) {
        deleteByProperty("portal_id", portalId);
    }

    @Override
    protected Class<PortalEnvironmentChangeRecord> getClazz() {
        return PortalEnvironmentChangeRecord.class;
    }
}
