package bio.terra.pearl.core.service.address;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AddressValidationClientProvider {

    // Maps the simple class name of the client an instance of that client
    // e.g. "AddressValidationClientStub" -> instance of that class
    private final Map<String, AddressValidationClient> clientMap = new HashMap<>();
    private final String defaultClient;

    public AddressValidationClientProvider(AddressValidationConfig addressValidationConfig,
                                           AddressValidationClientStub stubbedClient) {
        defaultClient = addressValidationConfig.getAddressValidationClass();

        clientMap.put("AddressValidationClientStub", stubbedClient);
    }

    public AddressValidationClient get() {
        return clientMap.get(defaultClient);
    }

    public AddressValidationClient get(String client) {
        return clientMap.get(client);
    }
}