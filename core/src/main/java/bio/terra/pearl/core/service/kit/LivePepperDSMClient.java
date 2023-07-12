package bio.terra.pearl.core.service.kit;

import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.participant.Enrollee;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

@Component
public class LivePepperDSMClient implements PepperDSMClient {
    private final PepperDSMConfig pepperDSMConfig;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public LivePepperDSMClient(PepperDSMConfig pepperDSMConfig,
                               WebClient.Builder webClientBuilder,
                               ObjectMapper objectMapper) {
        this.pepperDSMConfig = pepperDSMConfig;
        this.webClient = webClientBuilder
                .baseUrl(this.pepperDSMConfig.getBasePath())
                .build();
        this.objectMapper = objectMapper;
    }

    @Override
    public String sendKitRequest(Enrollee enrollee, KitRequest kitRequest, PepperKitAddress address)
            throws JsonProcessingException {
        var juniperKitRequest = PepperDSMKitRequest.JuniperKitRequest.builderWithAddress(address)
                .juniperKitId(kitRequest.getId().toString())
                .juniperParticipantId(enrollee.getShortcode())
                .build();
        var pepperDSMKitRequest = PepperDSMKitRequest.builder()
                .juniperKitRequest(juniperKitRequest)
                .kitType("SALIVA")
                .juniperStudyId("Juniper-mock-guid")
                .build();

        var body = objectMapper.writeValueAsString(pepperDSMKitRequest);
        Mono<String> stringMono = webClient
                .post().uri("/shipKit")
                .header("Authorization", "Bearer " + generateDsmJwt())
                .bodyValue(body)
                .retrieve().bodyToMono(String.class);
        return stringMono.block();
    }

    @Override
    public PepperKitStatus fetchKitStatus(UUID kitRequestId) {
        return null;
    }

    @Override
    public Collection<PepperKitStatus> fetchKitStatusByStudy(UUID studyId) {
        return null;
    }

    private String generateDsmJwt() {
        var fifteenMinutes = 15 * 60 * 1000;
        var now = System.currentTimeMillis();
        return JWT.create()
                .withExpiresAt(Instant.ofEpochMilli(now + fifteenMinutes))
                .sign(Algorithm.HMAC256(pepperDSMConfig.getSecret()));
    }
}

@Component
@Getter
@Setter
class PepperDSMConfig {
    private String basePath;
    private String secret;

    public PepperDSMConfig(Environment environment) {
        this.basePath = environment.getProperty("env.dsm.basePath");
        this.secret = environment.getProperty("env.dsm.secret");
    }
}
