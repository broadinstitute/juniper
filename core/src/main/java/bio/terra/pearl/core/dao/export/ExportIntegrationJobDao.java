package bio.terra.pearl.core.dao.export;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.export.ExportIntegrationJob;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class ExportIntegrationJobDao extends BaseMutableJdbiDao<ExportIntegrationJob> {

    public ExportIntegrationJobDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<ExportIntegrationJob> getClazz() {
        return ExportIntegrationJob.class;
    }

    public List<ExportIntegrationJob> findByExportIntegrationId(UUID integrationId) {
        return findAllByProperty("export_integration_id", integrationId);
    }

    public void deleteByExportIntegrationId(UUID integrationId) {
        deleteByProperty("export_integration_id", integrationId);
    }

    public void deleteByExportIntegrationIds(List<UUID> integrationIds) {
        deleteByProperty("export_integration_id", integrationIds);
    }
}
