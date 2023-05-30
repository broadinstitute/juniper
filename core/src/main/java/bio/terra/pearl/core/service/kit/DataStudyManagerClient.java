package bio.terra.pearl.core.service.kit;

import bio.terra.pearl.core.config.DataStudyManagerConfig;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.participant.Enrollee;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Component
public class DataStudyManagerClient {
    private final DataStudyManagerConfig dataStudyManagerConfig;
    private final WebClient webClient;

    public DataStudyManagerClient(DataStudyManagerConfig dataStudyManagerConfig,
                                  WebClient.Builder webClientBuilder) {
        this.dataStudyManagerConfig = dataStudyManagerConfig;
        this.webClient = webClientBuilder
                .baseUrl(this.dataStudyManagerConfig.getBasePath())
                .build();
    }

    public String sendKitRequest(Enrollee enrollee, KitRequest kitRequest, PepperKitAddress address) {
        var dataStudyManagerKitRequest = DataStudyManagerKitRequest.builderWithAddress(address)
                .juniperKitId(kitRequest.getId().toString())
                .juniperParticipantId(enrollee.getShortcode())
                .build();

        // TODO: call the correct endpoint; this is just a placeholder until the correct endpoint is available on dev
        Mono<String> stringMono = webClient
                .get().uri("/Kits/{kitId}", "12345678900301")
                .header("Authorization", "Bearer " + generateDsmJwt())
                .retrieve().bodyToMono(String.class);

        return stringMono.block();
    }

    private String generateDsmJwt() {
        var fifteenMinutes = 15 * 60 * 1000;
        var now = System.currentTimeMillis();
        return JWT.create()
                .withExpiresAt(Instant.ofEpochMilli(now + fifteenMinutes))
                .sign(Algorithm.HMAC256(dataStudyManagerConfig.getSecret()));
    }
}
