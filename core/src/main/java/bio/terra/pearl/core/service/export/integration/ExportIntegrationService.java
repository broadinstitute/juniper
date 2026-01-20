package bio.terra.pearl.core.service.export.integration;

import bio.terra.pearl.core.dao.export.ExportIntegrationDao;
import bio.terra.pearl.core.dao.export.ExportOptionsDao;
import bio.terra.pearl.core.model.audit.ResponsibleEntity;
import bio.terra.pearl.core.model.export.ExportDestinationType;
import bio.terra.pearl.core.model.export.ExportIntegration;
import bio.terra.pearl.core.model.export.ExportIntegrationJob;
import bio.terra.pearl.core.model.export.ExportOptions;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.publishing.ConfigChange;
import bio.terra.pearl.core.model.publishing.ConfigChangeList;
import bio.terra.pearl.core.model.publishing.ListChange;
import bio.terra.pearl.core.model.publishing.StudyEnvironmentChange;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.CrudService;
import bio.terra.pearl.core.service.export.ExportOptionsWithExpression;
import bio.terra.pearl.core.service.publishing.StudyEnvPublishable;
import bio.terra.pearl.core.service.search.EnrolleeSearchExpressionParser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class ExportIntegrationService extends CrudService<ExportIntegration, ExportIntegrationDao> implements StudyEnvPublishable {
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

    public void delete(UUID id) {
        ExportIntegration integration = find(id).orElseThrow(() -> new IllegalArgumentException("Export integration not found"));
        exportIntegrationJobService.deleteByExportIntegrationId(id);
        super.delete(id, CascadeProperty.EMPTY_SET);
        exportOptionsDao.delete(integration.getExportOptionsId());
    }

    public ExportIntegration update(ExportIntegration integration) {
        ExportOptions updatedOpts = exportOptionsDao.update(integration.getExportOptions());
        ExportIntegration newIntegration = super.update(integration);
        newIntegration.setExportOptions(updatedOpts);
        return newIntegration;
    }

    public void doAllExports(ResponsibleEntity operator) {
        List<ExportIntegration> integrations = dao.findAllActiveWithOptions();
        for ( int i = 0; i < integrations.size(); i++ ) {
            ExportIntegration integration = integrations.get(i);
            doExport(integration, operator);
            if (i < integrations.size() - 1) {
                try {
                    // Wait three minutes so that if related studies are exporting to the same table, Airtable can finish indexing
                    Thread.sleep(180 * 1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
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
            attachOptions(List.of(integration));
        });
        return integrationOpt;
    }

    public void attachOptions(List<ExportIntegration> integrations) {
        List<ExportOptions> options = exportOptionsDao.findAllPreserveOrder(integrations.stream().map(ExportIntegration::getExportOptionsId).toList());
        for (int i = 0; i < integrations.size(); i++) {
            integrations.get(i).setExportOptions(options.get(i));
        }
    }

    public void deleteByStudyEnvironmentId(UUID studyEnvId) {
        List<ExportIntegration> integrations = dao.findByStudyEnvironmentId(studyEnvId);
        List<UUID> integrationIds = integrations.stream().map(ExportIntegration::getId).toList();
        exportIntegrationJobService.deleteByExportIntegrationIds(integrationIds);
        dao.deleteByStudyEnvironmentId(studyEnvId);
        exportOptionsDao.deleteAll(integrations.stream().map(ExportIntegration::getExportOptionsId).toList());

    }

    @Override
    public void loadForPublishing(StudyEnvironment studyEnv) {
        List<ExportIntegration> integrations = findByStudyEnvironmentId(studyEnv.getId());
        attachOptions(integrations);
        studyEnv.setExportIntegrations(integrations);
    }

    @Override
    public void updateDiff(StudyEnvironmentChange change, StudyEnvironment sourceEnv, StudyEnvironment destEnv) {
        List<ExportIntegration> unmatchedIntegrations = new ArrayList<>(destEnv.getExportIntegrations());
        List<ExportIntegration> addedIntegrations = new ArrayList<>();
        List<ConfigChangeList<ExportIntegration>> changedIntegrations = new ArrayList<>();
        for (ExportIntegration sourceIntegration : sourceEnv.getExportIntegrations()) {
            ExportIntegration matchedIntegration = unmatchedIntegrations.stream().filter(
                            destIntegration -> Objects.equals(destIntegration.getName(), sourceIntegration.getName()))
                    .findAny().orElse(null);
            if (matchedIntegration == null) {
                addedIntegrations.add(sourceIntegration);
            } else {
                unmatchedIntegrations.remove(matchedIntegration);
                // we get changes from both the integration object and the child export options object
                List<ConfigChange> changes = ConfigChange.allChanges(sourceIntegration, matchedIntegration, getPublishIgnoreProps());
                changes.addAll(ConfigChange.allChanges(
                        sourceIntegration.getExportOptions(),
                        matchedIntegration.getExportOptions(),
                        getPublishIgnoreProps(),
                        "exportOptions"));
                if (!changes.isEmpty()) {
                    changedIntegrations.add(new ConfigChangeList<>(matchedIntegration, changes));
                }
            }
        }
        change.setExportIntegrationChanges(new ListChange<>(addedIntegrations, unmatchedIntegrations, changedIntegrations));
    }

    @Override
    @Transactional
    public void applyDiff(StudyEnvironmentChange change, StudyEnvironment destEnv, PortalEnvironment destPortalEnv) {
        for (ExportIntegration integration : change.getExportIntegrationChanges().addedItems()) {
            integration.cleanForCopying();
            integration.setStudyEnvironmentId(destEnv.getId());
            create(integration);
        }
        for (ExportIntegration integration : change.getExportIntegrationChanges().removedItems()) {
            delete(integration.getId(), CascadeProperty.EMPTY_SET);
        }
        for (ConfigChangeList<ExportIntegration> changedIntegration : change.getExportIntegrationChanges().changedItems()) {
            ExportIntegration destIntegration = changedIntegration.entity();
            for (ConfigChange configChange : changedIntegration.changes()) {
                configChange.apply(destIntegration);
            }
            update(destIntegration);
        }
    }
}
