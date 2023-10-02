package bio.terra.pearl.core.service.kit;

import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.participant.Enrollee;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.validation.Validator;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class LivePepperDSMClient implements PepperDSMClient {
    private static final Logger log = LoggerFactory.getLogger(LivePepperDSMClient.class);

    private final PepperDSMConfig pepperDSMConfig;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    public LivePepperDSMClient(PepperDSMConfig pepperDSMConfig,
                               WebClient.Builder webClientBuilder,
                               ObjectMapper objectMapper,
                               Validator validator) {
        this.pepperDSMConfig = pepperDSMConfig;
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    @Override
    public JsonNode sendKitRequest(String studyShortcode, Enrollee enrollee, KitRequest kitRequest, PepperKitAddress address) {
        var request = buildAuthedPostRequest("shipKit", makeKitRequestBody(studyShortcode, enrollee, kitRequest, address));
//        objectMapper.treeToValue()
//        return retrieveResponse(request);
        return retrieveAndDeserializeResponse(request, PepperKitStatusResponse.class);
    }

    @Override
    public JsonNode fetchKitStatus(UUID kitRequestId) {
        // TODO: change "juniperKit" to "juniperkit" after DSM updates this path
        var request = buildAuthedGetRequest("kitstatus/juniperKit/%s".formatted(kitRequestId));
        var response = retrieveAndDeserializeResponse(request, PepperKitStatusResponse.class);
        // TODO: move this
//        if (response.getKits().length != 1) {
//            throw new PepperException("Expected a single result from fetchKitStatus by ID (%s), got %d".formatted(
//                    kitRequestId, response.getKits().length));
//        }
        return response;
    }

    @Override
    public JsonNode fetchKitStatusByStudy(String studyShortcode) {
        var request = buildAuthedGetRequest("kitstatus/study/%s".formatted(makePepperStudyName(studyShortcode)));
        var response = retrieveAndDeserializeResponse(request, PepperKitStatusResponse.class);
//        log.info(response.toString());
        return response;
    }

    private String makePepperStudyName(String studyShortcode) {
        return "juniper-" + studyShortcode;
    }

    private String makeKitRequestBody(String studyShortcode, Enrollee enrollee, KitRequest kitRequest, PepperKitAddress address) {
        var juniperKitRequest = PepperDSMKitRequest.JuniperKitRequest.builderWithAddress(address)
                .juniperKitId(kitRequest.getId().toString())
                .juniperParticipantId(enrollee.getShortcode())
                .build();
        var pepperDSMKitRequest = PepperDSMKitRequest.builder()
                .juniperKitRequest(juniperKitRequest)
                .kitType(kitRequest.getKitType().getName())
                .juniperStudyId("juniper-%s".formatted(studyShortcode))
                .build();

        try {
            return objectMapper.writeValueAsString(pepperDSMKitRequest);
        } catch (JsonProcessingException e) {
            // Wrap in a RuntimeException to make it trigger rollback from @Transactional methods. There's no good
            // reason for PepperDSMKitRequest serialization to fail, so we can't assume that anything in the current
            // transaction is valid.
            throw new RuntimeException(e);
        }
    }

    private WebClient.RequestHeadersSpec<?> buildAuthedGetRequest(String path) {
        return webClient.get()
                .uri(buildUri(path))
                .header("Authorization", buildAuthorizationHeader());
    }

    private WebClient.RequestHeadersSpec<?> buildAuthedPostRequest(String path, String body) {
        return webClient.post()
                .uri(buildUri(path))
                .header("Authorization", buildAuthorizationHeader())
                .bodyValue(body);
    }

    private String buildUri(String path) {
        return "%s/%s".formatted(pepperDSMConfig.getBasePath(), path);
    }

    private String buildAuthorizationHeader() {
        return "Bearer " + generateDsmJwt();
    }

    private String generateDsmJwt() {
        var now = System.currentTimeMillis();
        var fifteenMinutes = 15 * 60 * 1000;
        return JWT.create()
                .withIssuer(pepperDSMConfig.getIssuerClaim())
                .withExpiresAt(Instant.ofEpochMilli(now + fifteenMinutes))
                .sign(Algorithm.HMAC256(pepperDSMConfig.getSecret()));
    }

    /**
     * Performs the request defined by the given WebClient specification and deserializes the response to the given
     * type.
     *
     * There are several potential sources of error here, including unchecked exceptions from 4xx/5xx response status
     * and JSON parsing and deserialization exceptions. This method centralizes the behavior of robustly handling any of
     * these possibilities and, when possible, converting them into a useful PepperException.
     */
    private <T extends PepperResponse> JsonNode retrieveAndDeserializeResponse(WebClient.RequestHeadersSpec<?> requestHeadersSpec, Class<T> clazz) {
        return retrieveBody(requestHeadersSpec)
                .flatMap(deserializeTo(clazz))
                .block();
    }

    private String retrieveResponse(WebClient.RequestHeadersSpec<?> requestHeadersSpec) {
        return retrieveBody(requestHeadersSpec)
                .block();
    }

    private Mono<String> retrieveBody(WebClient.RequestHeadersSpec<?> requestHeadersSpec) {
        return requestHeadersSpec
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        this::deserializePepperError)
                .bodyToMono(String.class)
                .map(body -> {
                    log.info(body);
                    return body;
                });
    }

    /**
     * Deserialize a PepperErrorResponse into a PepperException, robustly handling deserialization errors.
     */
    private Mono<PepperException> deserializePepperError(ClientResponse clientResponse) {
        // Read the body as a String and manually deserialize so we can capture and log the body if deserialization fails
        return clientResponse.bodyToMono(String.class)
                .flatMap(body -> {
                    try {
                        var pepperErrorResponse = objectMapper.readValue(body, PepperErrorResponse.class);
                        validate(pepperErrorResponse, PepperErrorResponse.class, body);
                        return Mono.error(new PepperException("Error from Pepper", pepperErrorResponse));
                    } catch (JsonProcessingException e) {
                        return Mono.error(new PepperException(
                                "Unable to parse error response from Pepper: %s".formatted(body), e));
                    }
                });
    }

    /**
     * Returns a function to deserialize a body String to the given type. If we were to use `bodyToMono(clazz)`
     * directly, deserialization failure would result in a `JsonProcessingException` and we would lose access to the
     * response body. Therefore, we do the deserialization manually so that we can capture the body for logging and
     * troubleshooting.
     */
    private <T extends PepperResponse> Function<String, Mono<JsonNode>> deserializeTo(Class<T> clazz) {
        return body -> {
            try {
                var jsonNode = objectMapper.readTree(body);
                if (!jsonNode.get("isError").asBoolean()) {
                    // TODO: move this? but will probably lose access to body
//                    validate(response, clazz, body);
                    return Mono.just(jsonNode);
                } else {
                    var error = objectMapper.treeToValue(jsonNode, PepperErrorResponse.class);
                    return Mono.error(new PepperException(
                            "Error response from Pepper: %s".formatted(body), error));
                }
            } catch (JsonProcessingException e) {
                return Mono.error(new PepperException(
                        "Unable to parse response from Pepper as %s: %s".formatted(clazz.getName(), body), e));
            }
        };
    }

    /** Validate returned object based on javax.validation annotations. */
    private <T> void validate(Object object, Class<T> clazz, String body) {
        var violations = validator.validate(object);
        if (!violations.isEmpty()) {
            throw new PepperException(
                    "Unexpected %s from Pepper: %s; %s".formatted(
                            clazz.getName(),
                            body,
                            violations.stream()
                                    .map(violation -> "%s %s".formatted(
                                            violation.getPropertyPath(),
                                            violation.getMessage()))
                                    .collect(Collectors.joining(", "))));
        }
    }

    @Component
    @Getter
    @Setter
    public static class PepperDSMConfig {
        @Accessors(fluent = true)
        private boolean useLiveDsm = false;
        private String basePath;
        private String issuerClaim;
        private String secret;

        public PepperDSMConfig(Environment environment) {
            this.useLiveDsm = environment.getProperty("env.dsm.useLiveDsm", Boolean.class, false);
            this.basePath = environment.getProperty("env.dsm.basePath");
            this.issuerClaim = environment.getProperty("env.dsm.issuerClaim");
            this.secret = environment.getProperty("env.dsm.secret");
        }
    }
}
