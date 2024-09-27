package bio.terra.pearl.core.service.export.integration;

import bio.terra.pearl.core.model.export.ExportIntegration;
import bio.terra.pearl.core.service.export.EnrolleeExportService;
import bio.terra.pearl.core.service.export.ExportOptionsWithExpression;
import bio.terra.pearl.core.service.search.EnrolleeSearchExpressionParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.ByteArrayOutputStream;

@Service
@Slf4j
public class AirtableExporter {
    private static final String AIRTABLE_API_KEY_NAME = "env.airtable.authToken";
    private final EnrolleeExportService enrolleeExportService;
    private final WebClient webClient;
    private final String apiKey;

    public AirtableExporter(EnrolleeExportService enrolleeExportService,
                            WebClient.Builder webClientBuilder,
                            Environment environment) {
        this.enrolleeExportService = enrolleeExportService;
        this.webClient = webClientBuilder.build();
        this.apiKey = environment.getProperty(AIRTABLE_API_KEY_NAME, "");
    }

    public void export(ExportIntegration integration, ExportOptionsWithExpression exportOpts) {
        // Export data to Airtable
        // first create a CSV, then upload it to Airtable
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        enrolleeExportService.export(exportOpts, integration.getStudyEnvironmentId(), baos);
        var postRequest = buildAuthedPostRequest(buildPath(integration), baos);
        postRequest.retrieve()
                .toBodilessEntity()
                .subscribe(
                        responseEntity -> {
                            // Handle success response here
                            log.info("Export to Airtable successful");
                            // handle response as necessary
                        },
                        error -> {
                            log.error("Export to Airtable failed");
                        });
    }

    private String buildPath(ExportIntegration integration) {
        return "https://api.airtable.com/" + integration.getDestinationUrl();
    }

    private WebClient.RequestHeadersSpec<?> buildAuthedPostRequest(String path, ByteArrayOutputStream baos) {
        return webClient.post()
                .uri(path)
                .header("Authorization", "Bearer " + apiKey)
                .bodyValue(baos.toString());
    }
}
