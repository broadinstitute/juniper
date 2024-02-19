package bio.terra.pearl.core.service.address;

import com.smartystreets.api.ClientBuilder;
import com.smartystreets.api.StaticCredentials;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Connects to the Smarty API for address validation of international and US addresses.
 */
@Component
public class SmartyClient {
    private com.smartystreets.api.us_street.Client usClient;
    private com.smartystreets.api.international_street.Client internationalClient;

    SmartyClient(AddressValidationConfig config) {
        ClientBuilder usBuilder = new ClientBuilder(
                new StaticCredentials(config.getAuthId(), config.getAuthToken())
        );
        ClientBuilder internationalBuilder = new ClientBuilder(
                new StaticCredentials(config.getAuthId(), config.getAuthToken())
        );
        usBuilder.withLicenses(List.of("us-core-cloud"));
        internationalBuilder.withLicenses(List.of("international-geocoding-cloud"));
        usClient = usBuilder.buildUsStreetApiClient();
        internationalClient = internationalBuilder.buildInternationalStreetApiClient();
    }

    void send(com.smartystreets.api.us_street.Lookup lookup) throws AddressValidationException {
        try {
            this.usClient.send(lookup);
        } catch (Exception e) {
            handleException(e);
            lookup.setResult(null);
        }
    }

    void send(com.smartystreets.api.international_street.Lookup lookup) throws AddressValidationException {
        try {
            this.internationalClient.send(lookup);
        } catch (Exception e) {
            handleException(e);
            lookup.setResult(null);
        }
    }

    void handleException(Exception e) throws AddressValidationException {
        for (String key : INVALID_ADDRESS_EXCEPTIONS) {
            if (e.getMessage().contains(key)) {
                return;
            }
        }
        throw new AddressValidationException(e.getMessage(), HttpStatus.valueOf(502));
    }

    private final List<String> INVALID_ADDRESS_EXCEPTIONS = List.of(
            "GET request lacked required fields", // this will be returned most commonly when someone misspells a country
            "GET request lacked a street field"
    );
}
