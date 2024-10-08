package bio.terra.pearl.core.service.export.integration;

import bio.terra.pearl.core.dao.export.ExportIntegrationJobDao;
import bio.terra.pearl.core.model.export.ExportIntegrationJob;
import bio.terra.pearl.core.service.CrudService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ExportIntegrationJobService extends CrudService<ExportIntegrationJob, ExportIntegrationJobDao> {
    public ExportIntegrationJobService(ExportIntegrationJobDao dao) {
        super(dao);
    }

    public List<ExportIntegrationJob> findByStudyEnvironment(UUID studyEnvironmentId) {
        return dao.findByStudyEnvironmentId(studyEnvironmentId);
    }

    public void deleteByExportIntegrationId(UUID integrationId) {
        dao.deleteByExportIntegrationId(integrationId);
    }

    public void deleteByExportIntegrationIds(List<UUID> integrationIds) {
        dao.deleteByExportIntegrationIds(integrationIds);
    }
}
