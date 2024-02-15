package bio.terra.pearl.core.service.address;

import com.smartystreets.api.ClientBuilder;
import com.smartystreets.api.StaticCredentials;
import com.smartystreets.api.exceptions.SmartyException;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Connects to the Smarty API for address validation of international and US addresses.
 */
@Component
public class SmartyClient {
    private com.smartystreets.api.us_street.Client usClient;
    private com.smartystreets.api.international_street.Client internationalClient;

    SmartyClient(AddressValidationConfig config) {
        ClientBuilder builder = new ClientBuilder(
                new StaticCredentials(config.getAuthId(), config.getAuthToken())
        );
        usClient = builder.buildUsStreetApiClient();
        internationalClient = builder.buildInternationalStreetApiClient();
    }

    void send(com.smartystreets.api.us_street.Lookup lookup) throws SmartyException, IOException, InterruptedException {
        this.usClient.send(lookup);
    }

    void send(com.smartystreets.api.international_street.Lookup lookup) throws SmartyException, IOException, InterruptedException {
        this.internationalClient.send(lookup);
    }
}
