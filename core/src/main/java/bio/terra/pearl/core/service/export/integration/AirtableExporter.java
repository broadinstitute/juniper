package bio.terra.pearl.core.service.export.integration;

import bio.terra.pearl.core.model.export.ExportIntegration;
import bio.terra.pearl.core.service.export.EnrolleeExportService;
import bio.terra.pearl.core.service.export.ExportOptionsParsed;
import bio.terra.pearl.core.service.search.EnrolleeSearchExpressionParser;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class AirtableExporter {
    private final EnrolleeExportService enrolleeExportService;
    private final EnrolleeSearchExpressionParser enrolleeSearchExpressionParser;

    public AirtableExporter(EnrolleeExportService enrolleeExportService,
                            EnrolleeSearchExpressionParser enrolleeSearchExpressionParser) {
        this.enrolleeExportService = enrolleeExportService;
        this.enrolleeSearchExpressionParser = enrolleeSearchExpressionParser;
    }

    public void export(ExportIntegration integration) {
        // Export data to Airtable
        // first create a CSV, then upload it to Airtable
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        ExportOptionsParsed parsedOpts = enrolleeSearchExpressionParser.parseExportOptions(integration.getExportOptions());
        enrolleeExportService.export();
    }
}
