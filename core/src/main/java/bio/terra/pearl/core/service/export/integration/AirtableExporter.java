package bio.terra.pearl.core.service.export.integration;

import bio.terra.pearl.core.model.export.ExportIntegration;
import bio.terra.pearl.core.service.export.EnrolleeExportService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.ByteArrayOutputStream;
import java.util.function.Consumer;

@Service
@Slf4j
public class AirtableExporter extends ExternalExporter {
    private static final String AIRTABLE_API_KEY_NAME = "env.airtable.authToken";
    private static final String AIRTABLE_BASE_URL = "https://api.airtable.com/";
    private final WebClient webClient;
    private final AirtableConfig config;

    public AirtableExporter(WebClient.Builder webClientBuilder, AirtableConfig config,
                            ExportIntegrationJobService exportIntegrationJobService,
                            EnrolleeExportService enrolleeExportService) {
        super(exportIntegrationJobService, enrolleeExportService);
        this.webClient = webClientBuilder.build();
        this.config = config;
    }

    protected void send(ExportIntegration integration, ByteArrayOutputStream outputStream,
                       Consumer<String> handleComplete, Consumer<Exception> handleError) {
        var postRequest = buildAuthedPostRequest(buildPath(integration), outputStream);
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
                .header("Authorization", "Bearer " + config.getAuthToken())
                .bodyValue(baos.toString());  // there'smight be  a way to have the webClient take the stream directly, but I couldn't find it easily
    }

    @Component @Getter @Setter
    public static class AirtableConfig {
        public AirtableConfig(Environment environment) {
            this.authToken = environment.getProperty(AIRTABLE_API_KEY_NAME, "");
        }
        private String authToken;
    }

}
