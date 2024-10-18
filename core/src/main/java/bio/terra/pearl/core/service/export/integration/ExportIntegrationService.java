package bio.terra.pearl.core.service.export.integration;

import bio.terra.pearl.core.dao.export.ExportIntegrationDao;
import bio.terra.pearl.core.dao.export.ExportOptionsDao;
import bio.terra.pearl.core.model.audit.ResponsibleEntity;
import bio.terra.pearl.core.model.export.ExportDestinationType;
import bio.terra.pearl.core.model.export.ExportIntegration;
import bio.terra.pearl.core.model.export.ExportIntegrationJob;
import bio.terra.pearl.core.model.export.ExportOptions;
import bio.terra.pearl.core.service.CrudService;
import bio.terra.pearl.core.service.export.ExportOptionsWithExpression;
import bio.terra.pearl.core.service.search.EnrolleeSearchExpressionParser;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class ExportIntegrationService extends CrudService<ExportIntegration, ExportIntegrationDao> {
    private final ExportIntegrationJobService exportIntegrationJobService;
    private final ExportOptionsDao exportOptionsDao;
    private final EnrolleeSearchExpressionParser enrolleeSearchExpressionParser;
    private final Map<ExportDestinationType, ExternalExporter> externalExporters;


    public ExportIntegrationService(ExportIntegrationDao dao,
                                    ExportIntegrationJobService exportIntegrationJobService,
                                    ExportOptionsDao exportOptionsDao,
                                    EnrolleeSearchExpressionParser enrolleeSearchExpressionParser,
                                    AirtableExporter airtableExporter) {
        super(dao);
        this.exportIntegrationJobService = exportIntegrationJobService;
        this.exportOptionsDao = exportOptionsDao;
        this.enrolleeSearchExpressionParser = enrolleeSearchExpressionParser;
        this.externalExporters = Map.of(ExportDestinationType.AIRTABLE, airtableExporter);
    }

    public ExportIntegration create(ExportIntegration integration) {
        ExportOptions newOptions = integration.getExportOptions() != null ? integration.getExportOptions() : new ExportOptions();
        newOptions = exportOptionsDao.create(newOptions);
        integration.setExportOptionsId(newOptions.getId());
        ExportIntegration newIntegration = super.create(integration);
        newIntegration.setExportOptions(newOptions);
        return newIntegration;
    }

    public ExportIntegration update(ExportIntegration integration) {
        ExportOptions updatedOpts = exportOptionsDao.update(integration.getExportOptions());
        ExportIntegration newIntegration = super.update(integration);
        newIntegration.setExportOptions(updatedOpts);
        return newIntegration;
    }

    public ExportIntegrationJob doExport(ExportIntegration integration, ResponsibleEntity operator) {
        ExternalExporter exporter = externalExporters.get(integration.getDestinationType());
        return doExport(exporter, integration, operator);
    }

    protected ExportIntegrationJob doExport(ExternalExporter exporter, ExportIntegration integration, ResponsibleEntity operator) {
        if (integration.getExportOptions() == null) {
            throw new IllegalArgumentException("Export options must be set to run an export integration");
        }
        ExportOptionsWithExpression parsedOpts = enrolleeSearchExpressionParser.parseExportOptions(integration.getExportOptions());
        ExportIntegrationJob job = ExportIntegrationJob.builder()
                .exportIntegrationId(integration.getId())
                .status(ExportIntegrationJob.Status.GENERATING)
                .creatingAdminUserId(operator.getAdminUser() != null ? operator.getAdminUser().getId() : null)
                .systemProcess(operator.getSystemProcess())
                .startedAt(Instant.now())
                .build();
        job = exportIntegrationJobService.create(job);
        exporter.export(integration, parsedOpts, job);
        return job;
    }



    public List<ExportIntegration> findByStudyEnvironmentId(UUID studyEnvId) {
        return dao.findByStudyEnvironmentId(studyEnvId);
    }

    public Optional<ExportIntegration> findWithOptions(UUID id) {
        Optional<ExportIntegration> integrationOpt = dao.find(id);
        integrationOpt.ifPresent(integration -> {
            integration.setExportOptions(exportOptionsDao.find(integration.getExportOptionsId()).orElse(null));
        });
        return integrationOpt;
    }

    public void deleteByStudyEnvironmentId(UUID studyEnvId) {
        List<ExportIntegration> integrations = dao.findByStudyEnvironmentId(studyEnvId);
        List<UUID> integrationIds = integrations.stream().map(ExportIntegration::getId).toList();
        exportIntegrationJobService.deleteByExportIntegrationIds(integrationIds);
        dao.deleteByStudyEnvironmentId(studyEnvId);
        exportOptionsDao.deleteAll(integrations.stream().map(ExportIntegration::getExportOptionsId).toList());

    }
}
