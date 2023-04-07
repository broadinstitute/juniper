package bio.terra.pearl.core.dao.publishing;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.publishing.PortalEnvironmentChangeRecord;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class PortalEnvironmentChangeRecordDao extends BaseJdbiDao<PortalEnvironmentChangeRecord> {
    public PortalEnvironmentChangeRecordDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<PortalEnvironmentChangeRecord> getClazz() {
        return PortalEnvironmentChangeRecord.class;
    }
}
