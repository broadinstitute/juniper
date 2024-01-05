package bio.terra.pearl.core.service.kit.pepper;

import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.service.exception.internal.InternalServerException;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class LivePepperDSMClient implements PepperDSMClient {
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
    public PepperKit sendKitRequest(String studyShortcode, Enrollee enrollee, KitRequest kitRequest, PepperKitAddress address)
    throws PepperApiException, PepperParseException {
        var request = buildAuthedPostRequest("shipKit", makeKitRequestBody(studyShortcode, enrollee, kitRequest, address));
        PepperKitStatusResponse response = retrieveAndDeserializeResponse(request, PepperKitStatusResponse.class);
        if (response.getKits().length != 1) {
            throw new PepperParseException("Expected a single result from shipKit by ID (%s), got %d".formatted(
                    kitRequest.getId(), response.getKits().length), response.getKits().toString(), response);
        }
        return response.getKits()[0];
    }

    @Override
    public PepperKit fetchKitStatus(UUID kitRequestId) throws PepperApiException, PepperParseException {
        var request = buildAuthedGetRequest("kitstatus/juniperKit/%s".formatted(kitRequestId));
        var response = retrieveAndDeserializeResponse(request, PepperKitStatusResponse.class);
        if (response.getKits().length != 1) {
            throw new PepperApiException("Expected a single result from fetchKitStatus by ID (%s), got %d".formatted(
                    kitRequestId, response.getKits().length));
        }
        return response.getKits()[0];
    }

    @Override
    public Collection<PepperKit> fetchKitStatusByStudy(String studyShortcode) throws PepperApiException, PepperParseException {
        var request = buildAuthedGetRequest("kitstatus/study/%s".formatted(makePepperStudyName(studyShortcode)));
        var response = retrieveAndDeserializeResponse(request, PepperKitStatusResponse.class);
        return Arrays.asList(response.getKits());
    }

    private String makePepperStudyName(String studyShortcode) {
        return "juniper-" + studyShortcode;
    }

    private String makeKitRequestBody(String studyShortcode, Enrollee enrollee, KitRequest kitRequest, PepperKitAddress address) {
        var juniperKitRequest = PepperDSMKitRequest.JuniperKitRequest.builderWithAddress(address)
                .juniperKitId(kitRequest.getId().toString())
                .juniperParticipantId(enrollee.getShortcode())
                .build();
        PepperDSMKitRequest pepperDSMKitRequest = PepperDSMKitRequest.builder()
                .juniperKitRequest(juniperKitRequest)
                .kitType(kitRequest.getKitType().getName())
                .juniperStudyId("juniper-%s".formatted(studyShortcode))
                .build();

        try {
            return objectMapper.writeValueAsString(pepperDSMKitRequest);
        } catch (JsonProcessingException e) {
            // There's no normal reason for PepperDSMKitRequest serialization to fail,
            // so if it fails, something very unexpected is happening
            throw new InternalServerException("Error serializing PepperDSMKitRequest", e);
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
     * exceptions from 4xx/5xx response status are thrown as PepperApiException
     * exceptions parsing the response are thrown as PepperParseExceptions
     */
    private <T> T retrieveAndDeserializeResponse(WebClient.RequestHeadersSpec<?> requestHeadersSpec, Class<T> clazz) {
        requestHeadersSpec.httpRequest(req -> log.info("Sending DSM request: {}", req.getURI()));

        return requestHeadersSpec
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        this::deserializePepperError)
                .bodyToMono(String.class)
                .flatMap(deserializeTo(clazz))
                .map(responseAndBody -> validate(responseAndBody, clazz))
                .map(responseAndBody -> responseAndBody.responseObj)
                .block();
    }

    /**
     * Deserialize a PepperErrorResponse into a PepperException, robustly handling deserialization errors.
     */
    private Mono<PepperApiException> deserializePepperError(ClientResponse clientResponse) {
        // Read the body as a String and manually deserialize so we can capture and log the body if deserialization fails
        return clientResponse.bodyToMono(String.class)
                .flatMap(deserializeTo(PepperErrorResponse.class))
                .onErrorMap(PepperParseException.class, e -> {
                    // error message that we can't parse at all
                    return new PepperApiException("Error from Pepper with unexpected format: %s".formatted(e.responseString),
                            clientResponse.statusCode());
                })
                .map(responseAndBody -> validate(responseAndBody, PepperErrorResponse.class))
                .onErrorMap(PepperParseException.class, e -> {
                    // error message that parses into a PepperErrorResponse, but with unexpected attributes
                    return new PepperApiException("Error from Pepper with unexpected format: %s".formatted(e.responseString),
                            (PepperErrorResponse) e.responseObj, clientResponse.statusCode());
                })
                .map(responseAndBody -> new PepperApiException(
                        responseAndBody.responseObj.getErrorMessage(),
                        responseAndBody.responseObj,
                        clientResponse.statusCode()));
    }

    /**
     * Returns a function to deserialize a body String to the given type. If we were to use `bodyToMono(clazz)`
     * directly, deserialization failure would result in a `JsonProcessingException` and we would lose access to the
     * response body. Therefore, we do the deserialization manually so that we can capture the body for logging and
     * troubleshooting.
     */
    private <T> Function<String, Mono<ResponseAndBody<T>>> deserializeTo(Class<T> clazz) throws PepperParseException {
        return body -> {
            // No deserialization needed if the requested response type is String
            if (clazz.equals(String.class)) {
                return Mono.just(new ResponseAndBody<T>(clazz.cast(body), body));
            }
            try {
                var object = objectMapper.readValue(body, clazz);
                return Mono.just(new ResponseAndBody<T>(object, body));
            } catch (JsonProcessingException e) {
                 throw new PepperParseException(
                        "Unable to parse response from Pepper as %s".formatted(clazz.getName()),
                         body);
            }
        };
    }

    /** Validate returned object based on javax.validation annotations. */
    private <T> ResponseAndBody<T> validate(ResponseAndBody<T> responseAndBody, Class<T> clazz) throws PepperParseException {
        var violations = validator.validate(responseAndBody.responseObj);
        if (!violations.isEmpty()) {
            String validationMsg = violations.stream()
                    .map(violation -> "%s %s".formatted(
                            violation.getPropertyPath(),
                            violation.getMessage()))
                    .collect(Collectors.joining(", "));
            throw new PepperParseException(
                    "Pepper response failed validation: %s".formatted(clazz.getName(), validationMsg), responseAndBody.body, responseAndBody.responseObj);

        }
        return responseAndBody;
    }

    @AllArgsConstructor
    public static class ResponseAndBody<T> {
        public T responseObj;
        public String body;
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
