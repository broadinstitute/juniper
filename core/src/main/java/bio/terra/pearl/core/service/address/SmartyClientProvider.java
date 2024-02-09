package bio.terra.pearl.core.service.address;

import com.smartystreets.api.ClientBuilder;
import com.smartystreets.api.StaticCredentials;
import org.springframework.stereotype.Component;

@Component
public class SmartyClientProvider {

    private final StaticCredentials credentials;

    public SmartyClientProvider(AddressValidationConfig config) {
            credentials = new StaticCredentials(config.getAuthId(),
                config.getAuthToken());
    }

    public com.smartystreets.api.us_street.Client usClient() {
        return new ClientBuilder(credentials).buildUsStreetApiClient();
    }

    public com.smartystreets.api.international_street.Client internationalClient() {
        return new ClientBuilder(credentials).buildInternationalStreetApiClient();
    }

}
