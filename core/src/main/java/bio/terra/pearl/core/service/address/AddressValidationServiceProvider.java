package bio.terra.pearl.core.service.address;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AddressValidationServiceProvider {

    // Maps the simple class name of the client an instance of that client
    // e.g. "AddressValidationClientStub" -> instance of that class
    private final Map<String, AddressValidationService> clientMap = new HashMap<>();
    private final String defaultClient;

    public AddressValidationServiceProvider(AddressValidationConfig addressValidationConfig,
                                            AddressValidationServiceStub stubbedClient,
                                            SmartyAddressValidationService realClient) {
        defaultClient = addressValidationConfig.getAddressValidationClass();

        clientMap.put("AddressValidationClientStub", stubbedClient);
        clientMap.put("SmartyAddressValidationClient", realClient);
    }

    public AddressValidationService get() {
        return clientMap.get(defaultClient);
    }

    public AddressValidationService get(String client) {
        return clientMap.get(client);
    }
}
