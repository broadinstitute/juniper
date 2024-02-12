package bio.terra.pearl.core.service.address;

import bio.terra.pearl.core.model.address.AddressComponent;
import bio.terra.pearl.core.model.address.AddressValidationResultDto;
import bio.terra.pearl.core.model.address.MailingAddress;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Validation client which, depending upon the provided country, will either
 * map to the US Smarty Client or the International Client.
 */
@Component
@Slf4j
public class SmartyAddressValidationService implements AddressValidationService {

    SmartyUSAddressValidationService usClient;
    SmartyInternationalAddressValidationService internationalClient;

    SmartyAddressValidationService(SmartyUSAddressValidationService usClient) {
        this.usClient = usClient;
    }

    @Override
    public AddressValidationResultDto validate(MailingAddress address) {
        List<AddressComponent> missing = missingFields(address);
        if (!missing.isEmpty()) {
            return AddressValidationResultDto
                    .builder()
                    .valid(false)
                    .invalidComponents(missing)
                    .build();
        }

        if (POSSIBLE_US_SPELLINGS.contains(address.getCountry())) {
            return this.usClient.validate(address);
        }

        return this.internationalClient.validate(address);

    }

    private List<AddressComponent> missingFields(MailingAddress address) {
        List<AddressComponent> missing = new ArrayList<>();
        if (StringUtils.isEmpty(address.getStreet1())) {
            missing.add(AddressComponent.STREET_NAME);
            missing.add(AddressComponent.STREET_TYPE);
            missing.add(AddressComponent.HOUSE_NUMBER);
        }
        if (StringUtils.isEmpty(address.getCountry())) {
            missing.add(AddressComponent.COUNTRY);
        }
        return missing;
    }


    private static final List<String> POSSIBLE_US_SPELLINGS = List.of(
            "US",
            "USA",
            "United States",
            "U.S.A.",
            "United States of America"
    );

}
