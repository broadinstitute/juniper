package bio.terra.pearl.core.service.export.integration;

import bio.terra.pearl.core.dao.export.ExportIntegrationDao;
import bio.terra.pearl.core.model.export.ExportDestinationType;
import bio.terra.pearl.core.model.export.ExportIntegration;
import bio.terra.pearl.core.service.CrudService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ExportIntegrationService extends CrudService<ExportIntegration, ExportIntegrationDao> {
    private final ExportIntegrationJobService exportIntegrationJobService;

    public ExportIntegrationService(ExportIntegrationDao dao,
                                    ExportIntegrationJobService exportIntegrationJobService) {
        super(dao);
        this.exportIntegrationJobService = exportIntegrationJobService;
    }

    public Object run(ExportIntegration integration) {
        if (ExportDestinationType.AIRTABLE.equals(integration.getDestinationType())) {

        }
        return null;
    }

    public List<ExportIntegration> findByStudyEnvironmentId(UUID studyEnvId) {
        return dao.findByStudyEnvironmentId(studyEnvId);
    }

    public void deleteByStudyEnvironmentId(UUID studyEnvId) {
        List<UUID> integrationIds = dao.findByStudyEnvironmentId(studyEnvId).stream().map(ExportIntegration::getId).toList();
        exportIntegrationJobService.deleteByExportIntegrationIds(integrationIds);
        dao.deleteByStudyEnvironmentId(studyEnvId);
    }
}
