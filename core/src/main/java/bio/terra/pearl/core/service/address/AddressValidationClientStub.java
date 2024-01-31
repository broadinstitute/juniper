package bio.terra.pearl.core.service.address;

import bio.terra.pearl.core.model.address.AddressValidationResultDto;
import bio.terra.pearl.core.model.participant.MailingAddress;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;

import java.util.List;

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
     * - Put the word "BAD" in street1
     * - All the tokens in street1 will be considered unresolved
     * - Example missing component data will be returned
     * - Real-world responses are not guaranteed have missingComponents or unresolvedTokens
     * <br>
     * State 2: Improvable address
     * - Put the word "IMPROVABLE" in street1
     * - Returns valid = true with a suggested address of 415 Main St.
     * <br>
     * State 3: Server side error
     * - Put the word "ERROR" in street1
     * - Throws an AddressValidationException
     * - HTTP Status code defaults to 404, but will use a valid status code if present in postalCode
     * <br>
     * State 4: Valid address
     * - All other addresses will default to valid
     */
    @Override
    public AddressValidationResultDto validate(MailingAddress address) throws AddressValidationException {

        if (address.getStreet1().contains(BAD_ADDRESS_INDICATOR)) {
            return AddressValidationResultDto
                    .builder()
                    .valid(false)
                    .missingComponents(List.of("street", "country", "postal_code")) // junk example data
                    .unresolvedTokens(List.of(StringUtils.split(address.getStreet1())))
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
                                    .build())
                    .build();
        } else if (address.getStreet1().contains(ERROR_INDICATOR)) {
            int errorCode = 404;
            try {
                errorCode = Integer.parseInt(address.getPostalCode());
                // keep error within bounds
                if (errorCode < 400 || errorCode > 599) {
                    errorCode = 404;
                }
            } catch (Exception e) {
                // ignore; default to 404
            }
            throw new AddressValidationException(
                    "Failed to run address validation",
                    HttpStatusCode.valueOf(errorCode));

        }


        return AddressValidationResultDto.builder().valid(true).build();
    }
}
