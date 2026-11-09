package bio.terra.pearl.core.service.kit.sidr;

import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.study.StudyEnvironmentConfig;
import bio.terra.pearl.core.service.kit.sidr.models.SIDREnvironment;
import bio.terra.pearl.core.service.kit.sidr.models.SIDROrder;
import bio.terra.pearl.core.shared.ApplicationRoutingPaths;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.IdTokenCredentials;
import com.google.auth.oauth2.IdTokenProvider;
import com.google.auth.oauth2.ImpersonatedCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles only the HTTP communication with the SIDR API. Requests are authenticated as the Juniper service account,
 * using a Google ID token with the SIDR environment's audience. When deployed, the token comes from the application
 * default credentials; in local environments those are the developer's, so the service account is impersonated.
 * Deciding what to order is the job of {@link SIDRService}.
 */
@Component
@Slf4j
public class LiveSIDRClient implements SIDRClient {
    public static final String ACTOR_EMAIL_HEADER = "Actor-Email";
    /**
     * the service account that local environments impersonate, since SIDR knows the service account, not people
     */
    private static final String LOCAL_IMPERSONATED_SERVICE_ACCOUNT =
            "juniper-app-gsa@broad-juniper-dev.iam.gserviceaccount.com";

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    // one set of credentials per audience (i.e. SIDR environment); they refresh their own tokens as needed
    private final Map<String, IdTokenCredentials> credentialsByAudience = new ConcurrentHashMap<>();
    private final ApplicationRoutingPaths routingPaths;

    public LiveSIDRClient(WebClient.Builder webClientBuilder, ObjectMapper objectMapper, ApplicationRoutingPaths routingPaths) {
        this.objectMapper = objectMapper;
        ConnectionProvider provider = ConnectionProvider.builder("sidr-connection-provider")
                .maxConnections(100)
                .maxIdleTime(Duration.ofSeconds(20))
                .maxLifeTime(Duration.ofSeconds(500))
                .pendingAcquireTimeout(Duration.ofSeconds(60))
                .evictInBackground(Duration.ofSeconds(120)).build();
        this.webClient = webClientBuilder
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create(provider)))
                .build();
        this.routingPaths = routingPaths;
    }

    @Override
    public SIDROrder postOrder(StudyEnvironmentConfig config, AdminUser actor, SIDROrder order) throws SIDRApiException {
        if (actor == null || actor.getUsername() == null || actor.getUsername().isBlank()) {
            // SIDR rejects service-account order submissions that don't say who the order is on behalf of
            throw new IllegalArgumentException("Kit order requires a user with an email");
        }
        SIDREnvironment env = config.getSidrEnvironment();
        String body;
        try {
            body = objectMapper.writeValueAsString(order);
        } catch (JsonProcessingException e) {
            // there's no normal reason for serialization to fail, so something very unexpected is happening
            throw new SIDRApiException("Could not place kit order", e);
        }
        WebClient.RequestHeadersSpec<?> request = webClient.post()
                .uri(buildUri(env, "api/order"))
                .header(HttpHeaders.AUTHORIZATION, buildAuthorizationHeader(env))
                .header(ACTOR_EMAIL_HEADER, actor.getUsername())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body);
        return retrieveOrder(request);
    }

    @Override
    public SIDROrder getOrder(StudyEnvironmentConfig config, String sidrOrderId) throws SIDRApiException {
        SIDREnvironment env = config.getSidrEnvironment();
        WebClient.RequestHeadersSpec<?> request = webClient.get()
                .uri(buildUri(env, "api/order/" + sidrOrderId))
                .header(HttpHeaders.AUTHORIZATION, buildAuthorizationHeader(env));
        return retrieveOrder(request);
    }

    /** the base url of the SIDR API for the environment. Overridable so tests can point at a mock server */
    protected String buildUri(SIDREnvironment env, String path) {
        return "%s/%s".formatted(env.getAPIURl(), path);
    }

    /** fetches a Google ID token for the given audience, as the default application credentials */
    protected String fetchIdToken(String audience) throws IOException {
        IdTokenCredentials creds = credentialsByAudience.computeIfAbsent(audience, aud -> {
            try {
                return createIdTokenCredentials(aud);
            } catch (IOException e) {
                throw new SIDRApiException("Could not place kit order", e);
            }
        });
        creds.refreshIfExpired();
        return creds.getIdToken().getTokenValue();
    }

    private String buildAuthorizationHeader(SIDREnvironment env) {
        try {
            return "Bearer " + fetchIdToken(env.getAPIAudience());
        } catch (IOException | IllegalArgumentException e) {
            throw new SIDRApiException("Could not place kit order", e);
        }
    }

    /** Performs the request and parses the response as an order. A response that can't be parsed is a failure. */
    private SIDROrder retrieveOrder(WebClient.RequestHeadersSpec<?> request) {
        request.httpRequest(req -> log.info("Sending SIDR request: {} {}", req.getMethod(), req.getURI()));
        String body = request
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        this::toApiException)
                .bodyToMono(String.class)
                .block();
        try {
            return objectMapper.readValue(body, SIDROrder.class);
        } catch (JsonProcessingException | IllegalArgumentException e) {
            log.error("Unable to parse SIDR response as an order: {}", body, e);
            throw new SIDRApiException("Could not place kit order", e);
        }
    }

    private Mono<SIDRApiException> toApiException(ClientResponse response) {
        return response.bodyToMono(String.class).defaultIfEmpty("").map(body -> {
            log.warn("Error response from SIDR: {} {}", response.statusCode(), body);
            return SIDRApiException.fromResponse(response.statusCode(), body);
        });
    }

    private IdTokenCredentials createIdTokenCredentials(String audience) throws IOException {
        GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
        IdTokenProvider idTokenProvider;
        if ("local".equals(routingPaths.getDeploymentZone())) {
            idTokenProvider = ImpersonatedCredentials.newBuilder()
                    .setSourceCredentials(credentials)
                    .setTargetPrincipal(LOCAL_IMPERSONATED_SERVICE_ACCOUNT)
                    .setScopes(List.of("https://www.googleapis.com/auth/cloud-platform"))
                    .build();
        } else if (credentials instanceof IdTokenProvider provider) {
            idTokenProvider = provider;
        } else {
            throw new IllegalArgumentException("Credentials are not an instance of IdTokenProvider.");
        }
        return IdTokenCredentials.newBuilder()
                .setIdTokenProvider(idTokenProvider)
                .setTargetAudience(audience)
                .build();
    }
}
