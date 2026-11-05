package bio.terra.pearl.core.service.kit.sidr;

import bio.terra.pearl.core.service.kit.sidr.models.SIDREnvironment;
import bio.terra.pearl.core.service.kit.sidr.models.SIDROrder;
import bio.terra.pearl.core.shared.GoogleServiceAccountUtils;
import com.google.auth.oauth2.IdTokenCredentials;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;

public class LiveSIDRClient implements SIDRClient{


    @Override
    public SIDROrder postOrder(SIDREnvironment env, SIDROrder sidrOrder) throws IOException {
        return null;
    }

    @Override
    public SIDROrder getOrder(SIDREnvironment env, String sidrOrderId) {
        return null;
    }

    private WebClient.RequestHeadersSpec<?> buildAuthedPostRequest(SIDREnvironment env, String path, String body) {
        return webClient.post()
                .uri(buildUri(env, path))
                .header("Authorization", buildAuthorizationHeader())
                .bodyValue(body);
    }

    private String buildUri(SIDREnvironment env, String path) {
        return "%s/%s".formatted(env.getAPIURl(), path);
    }


    private String buildAuthorizationHeader(SIDREnvironment env) throws IOException {
        IdTokenCredentials creds = GoogleServiceAccountUtils.getIdTokenCreds(env.getAPIAudience());
        return "Bearer " + creds.getIdToken().getTokenValue();
    }
}
