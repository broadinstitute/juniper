package bio.terra.pearl.core.service.migration;

import bio.terra.pearl.core.model.migration.AuthMigrationConfig;
import com.auth0.client.auth.AuthAPI;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.auth.TokenHolder;
import com.auth0.net.Response;
import com.auth0.net.TokenRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LegacyAuth0ProviderService implements LegacyAuthProviderService {

    public boolean attemptLogIn(
            AuthMigrationConfig config,
            String email,
            String password
    ) {

        try {
            AuthAPI authAPI = AuthAPI.newBuilder(
                    config.getDomain(),
                    config.getClientId(),
                    config.getClientSecret()).build();

            TokenRequest request = authAPI.login(email, password.toCharArray());

            Response<TokenHolder> holder = request.execute();

            if (holder.getStatusCode() != 200) {
                log.error("Auth0 login failed with status code: {}", holder.getStatusCode());
                return false;
            }

            return true;
        } catch (Auth0Exception auth0Exception) {
            return false;
        }

    }
}
