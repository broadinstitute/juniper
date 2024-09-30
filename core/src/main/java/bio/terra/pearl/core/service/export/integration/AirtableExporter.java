package bio.terra.pearl.core.service.export.integration;

import bio.terra.pearl.core.model.export.ExportIntegration;
import bio.terra.pearl.core.model.export.ExportIntegrationJob;
import bio.terra.pearl.core.service.export.EnrolleeExportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.ByteArrayOutputStream;
import java.util.function.Consumer;

@Service
@Slf4j
public class AirtableExporter extends ExternalExporter {
    private static final String AIRTABLE_API_KEY_NAME = "env.airtable.authToken";
    private static final String AIRTABLE_BASE_URL = "https://api.airtable.com/";
    private final ExportIntegrationJobService exportIntegrationJobService;
    private final EnrolleeExportService enrolleeExportService;

    private final WebClient webClient;
    private final String apiKey;

    public AirtableExporter(ExportIntegrationJobService exportIntegrationJobService,
                            EnrolleeExportService enrolleeExportService, WebClient.Builder webClientBuilder,
                            Environment environment) {
        super(exportIntegrationJobService, enrolleeExportService);
        this.exportIntegrationJobService = exportIntegrationJobService;
        this.enrolleeExportService = enrolleeExportService;
        this.webClient = webClientBuilder.build();
        this.apiKey = environment.getProperty(AIRTABLE_API_KEY_NAME, "");
    }

    protected void send(ExportIntegration integration, ByteArrayOutputStream baos,
                       Consumer<String> handleComplete, Consumer<Exception> handleError) {
        var postRequest = buildAuthedPostRequest(buildPath(integration), baos);
        postRequest.retrieve()
                .toBodilessEntity()
                .subscribe(
                        responseEntity -> {
                            handleComplete.accept("success"); // we can put detailed parsing of the response here if needed
                        },
                        error -> {
                            handleError.accept(new RuntimeException(error));
                        });
    }

    private String buildPath(ExportIntegration integration) {
        return AIRTABLE_BASE_URL + integration.getDestinationUrl();
    }

    private WebClient.RequestHeadersSpec<?> buildAuthedPostRequest(String path, ByteArrayOutputStream baos) {
        return webClient.post()
                .uri(path)
                .header("Authorization", "Bearer " + apiKey)
                .bodyValue(baos.toString());  // there'smight be  a way to have the webClient take the stream directly, but I couldn't find it easily
    }

}
