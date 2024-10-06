    package bio.terra.pearl.core.service.export.integration;

import bio.terra.pearl.core.model.export.ExportIntegration;
import bio.terra.pearl.core.model.export.ExportIntegrationJob;
import bio.terra.pearl.core.service.export.EnrolleeExportService;
import bio.terra.pearl.core.service.export.ExportOptionsWithExpression;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;

import java.io.ByteArrayOutputStream;
import java.util.function.Consumer;

@Slf4j
public abstract class ExternalExporter {

    private final ExportIntegrationJobService exportIntegrationJobService;

    private final EnrolleeExportService enrolleeExportService;

    protected abstract void send(ExportIntegration integration, ByteArrayOutputStream outputStream, Consumer<String> handleComplete, Consumer<Exception> handleError);

    public ExternalExporter(ExportIntegrationJobService exportIntegrationJobService, EnrolleeExportService enrolleeExportService) {
        this.exportIntegrationJobService = exportIntegrationJobService;
        this.enrolleeExportService = enrolleeExportService;
    }

    protected ByteArrayOutputStream createExportStream(ExportIntegration integration, ExportOptionsWithExpression parsedOpts) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        enrolleeExportService.export(parsedOpts, integration.getStudyEnvironmentId(), baos);
        return baos;
    }

    @Async
    public void export(ExportIntegration integration, ExportOptionsWithExpression parsedOpts, ExportIntegrationJob job) {
        try {
            job.setStatus(ExportIntegrationJob.Status.GENERATING);
            job = exportIntegrationJobService.update(job);

            ByteArrayOutputStream outputStream = createExportStream(integration, parsedOpts);

            job.setStatus(ExportIntegrationJob.Status.SENDING);
            final ExportIntegrationJob updatedJob = exportIntegrationJobService.update(job);

            send(integration, outputStream,
                    (String msg) -> handleComplete(updatedJob, integration, msg),
                    (Exception e) -> handleError(updatedJob, integration, e));
        } catch (Exception e) {
            handleError(job, integration, e);
        }
    }

    public void handleComplete(ExportIntegrationJob job, ExportIntegration integration, String message) {
        log.info("Export job complete: integration id: %s, job id: %s, message: %s".formatted(integration.getId(), job.getId(), message));
        job.setStatus(ExportIntegrationJob.Status.COMPLETE);
        job.setResult(message);
        exportIntegrationJobService.update(job);
    }

    public void handleError(ExportIntegrationJob job, ExportIntegration integration, Exception e) {
        log.error("Export job failed: integration id: %s, job id: %s".formatted(integration.getId(), job.getId()), e);
        job.setStatus(ExportIntegrationJob.Status.FAILED);
        job.setResult(e.getMessage());
        exportIntegrationJobService.update(job);
    }
}
