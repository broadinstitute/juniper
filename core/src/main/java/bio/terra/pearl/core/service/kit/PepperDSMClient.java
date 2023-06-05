package bio.terra.pearl.core.service.kit;

import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.participant.Enrollee;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import lombok.Getter;
import lombok.Setter;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Component
public class PepperDSMClient {
    private final PepperDSMConfig pepperDSMConfig;
    private final WebClient webClient;

    public PepperDSMClient(PepperDSMConfig pepperDSMConfig,
                           WebClient.Builder webClientBuilder) {
        this.pepperDSMConfig = pepperDSMConfig;
        this.webClient = webClientBuilder
                .baseUrl(this.pepperDSMConfig.getBasePath())
                .build();
    }

    public String sendKitRequest(Enrollee enrollee, KitRequest kitRequest, PepperKitAddress address) {
        var pepperDSMKitRequest = PepperDSMKitRequest.builderWithAddress(address)
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
