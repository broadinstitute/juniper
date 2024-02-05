package bio.terra.pearl.core.service.address;

import bio.terra.pearl.core.model.address.AddressComponent;
import bio.terra.pearl.core.model.address.AddressValidationResultDto;
import bio.terra.pearl.core.model.address.MailingAddress;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class AddressValidationClientStub implements AddressValidationClient {

    private static final String BAD_ADDRESS_INDICATOR = "BAD";
    private static final String IMPROVABLE_ADDRESS_INDICATOR = "IMPROVABLE";
    private static final String ERROR_INDICATOR = "ERROR";

    /**
     * Stubbed client allows you to test various possible states of address validation.
     * <br>
     * State 1: Invalid address
     * - Put the word "BAD" in street1, e.g. 123 Bad St
     * - All the tokens in street1 will be considered unresolved
     * - Example missing component data will be returned
     * - Real-world responses are not guaranteed have missingComponents or unresolvedTokens
     * <br>
     * State 2: Improvable address
     * - Put the word "IMPROVABLE" in street1, e.g. 415 IMPROVABLE St
     * - Returns valid = true with a suggested address of 415 Main St
     * <br>
     * State 3: Server side error
     * - Put the word "ERROR" in street1, e.g. 500 ERROR Lane
     * - Throws an AddressValidationException
     * - HTTP Status code defaults to 500, but will use a valid 400-599 status code if present in postalCode
     * <br>
     * State 4: Valid address
     * - All other addresses will default to valid
     */
    @Override
    public AddressValidationResultDto validate(UUID sessionId, MailingAddress address) throws AddressValidationException {

        if (address.getStreet1().contains(BAD_ADDRESS_INDICATOR)) {
            return AddressValidationResultDto
                    .builder()
                    .valid(false)
                    .missingComponents(findMissingComponents(address))
                    .unresolvedTokens(List.of(StringUtils.split(address.getStreet1())))
                    .sessionId(sessionId)
                    .build();
        } else if (address.getStreet1().contains(IMPROVABLE_ADDRESS_INDICATOR)) {
            return AddressValidationResultDto
                    .builder()
                    .valid(true)
                    .suggestedAddress(
                            MailingAddress
                                    .builder()
                                    .street1("415 Main St")
                                    .city("Cambridge")
                                    .state("MA")
                                    .country("USA")
                                    .postalCode("02142")
                                    .createdAt(null)
                                    .lastUpdatedAt(null)
                                    .build())
                    .sessionId(sessionId)
                    .build();
        } else if (address.getStreet1().contains(ERROR_INDICATOR)) {
            int errorCode = 500;
            try {
                errorCode = Integer.parseInt(address.getPostalCode());
                // keep error within bounds
                if (errorCode < 400 || errorCode > 599) {
                    errorCode = 500;
                }
            } catch (Exception e) {
                // ignore; default to 404
            }
            throw new AddressValidationException(
                    "Failed to run address validation",
                    HttpStatusCode.valueOf(errorCode));

        }


        return AddressValidationResultDto.builder().valid(true).sessionId(sessionId).build();
    }

    private List<AddressComponent> findMissingComponents(MailingAddress addr) {
        List<AddressComponent> missingComponents = new ArrayList<>();

        if (StringUtils.isEmpty(addr.getStreet1())) {
            missingComponents.add(AddressComponent.STREET_NAME);
            missingComponents.add(AddressComponent.STREET_TYPE);
            missingComponents.add(AddressComponent.HOUSE_NUMBER);
        }

        if (StringUtils.isEmpty(addr.getPostalCode())) {
            missingComponents.add(AddressComponent.POSTAL_CODE);
        }

        if (StringUtils.isEmpty(addr.getCity())) {
            missingComponents.add(AddressComponent.CITY);
        }

        if (StringUtils.isEmpty(addr.getCountry())) {
            missingComponents.add(AddressComponent.COUNTRY);
        }

        if (StringUtils.isEmpty(addr.getState())) {
            missingComponents.add(AddressComponent.STATE_PROVINCE);
        }

        return missingComponents;
    }

}
