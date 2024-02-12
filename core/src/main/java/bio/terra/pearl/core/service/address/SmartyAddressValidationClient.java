package bio.terra.pearl.core.service.address;

import bio.terra.pearl.core.model.address.AddressValidationResultDto;
import bio.terra.pearl.core.model.address.MailingAddress;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Validation client which, depending upon the provided country, will either
 * map to the US Smarty Client or (TODO: JN-864) the International Client.
 */
@Component
@Slf4j
public class SmartyAddressValidationClient implements AddressValidationClient {

    SmartyUSAddressValidationClient usClient;

    SmartyAddressValidationClient(SmartyUSAddressValidationClient usClient) {
        this.usClient = usClient;
    }

    @Override
    public AddressValidationResultDto validate(MailingAddress address) {
        return this.usClient.validate(address);
    }


}
