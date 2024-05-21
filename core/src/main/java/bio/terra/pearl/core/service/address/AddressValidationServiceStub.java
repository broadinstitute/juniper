package bio.terra.pearl.core.service.address;

import bio.terra.pearl.core.model.address.AddressComponent;
import bio.terra.pearl.core.model.address.AddressValidationResultDto;
import bio.terra.pearl.core.model.address.MailingAddress;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class AddressValidationServiceStub implements AddressValidationService {

    private static final String BAD_ADDRESS_INDICATOR = "BAD";
    private static final String INVALID_INDICATOR = "INVALID";
    private static final String IMPROVABLE_ADDRESS_INDICATOR = "IMPROVABLE";
    private static final String INFERENCE_ADDRESS_INDICATOR = "INFERENCE";
    private static final String ERROR_INDICATOR = "ERROR";
    private static final String VACANT_INDICATOR = "VACANT";

    /**
     * Stubbed client allows you to test various possible states of address validation. In all cases, if you put
     * "VACANT" in street1, the address will be considered vacant.
     * <br>
     * State 1: Invalid address
     * - Put the word "BAD" in street1, e.g. 123 Bad St, or leave it empty
     * - All the tokens in street1 will be considered unresolved
     * - Example missing component data will be returned based on simple rules
     * - Real-world responses are not guaranteed have missingComponents or unresolvedTokens
     * <br>
     * State 2: Improvable address
     * - Put the word "IMPROVABLE" in street1, e.g. 415 IMPROVABLE St
     * - Returns valid = true with a suggested address of 415 Main St
     * <br>
     * State 3: Required inference
     * - Put the word "INFERENCE" in street1, e.g. 415 INFERENCE St
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
    public AddressValidationResultDto validate(MailingAddress address) throws AddressValidationException {

        if (StringUtils.isEmpty(address.getStreet1()) || address.getStreet1().contains(BAD_ADDRESS_INDICATOR)) {
            return AddressValidationResultDto
                    .builder()
                    .valid(false)
                    .invalidComponents(findMissingComponents(address))
                    .vacant(isVacant(address))
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
                                    .country("US")
                                    .postalCode("02142-1027")
                                    .createdAt(null)
                                    .lastUpdatedAt(null)
                                    .build())
                    .hasInferredComponents(false)
                    .vacant(isVacant(address))
                    .build();
        } else if (address.getStreet1().contains(INFERENCE_ADDRESS_INDICATOR)) {
            return AddressValidationResultDto
                    .builder()
                    .valid(true)
                    .suggestedAddress(
                            MailingAddress
                                    .builder()
                                    .street1("415 Main St")
                                    .city("Cambridge")
                                    .state("MA")
                                    .country("US")
                                    .postalCode("02142-1027")
                                    .createdAt(null)
                                    .lastUpdatedAt(null)
                                    .build())
                    .hasInferredComponents(true)
                    .vacant(isVacant(address))
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
                // ignore; default to 500
            }
            throw new AddressValidationException(
                    "Failed to run address validation",
                    HttpStatus.valueOf(errorCode));
        }

        return AddressValidationResultDto
                .builder()
                .valid(true)
                .vacant(isVacant(address))
                .build();
    }

    private boolean isVacant(MailingAddress addr) {
        if (Objects.isNull(addr.getStreet1())) {
            return false;
        }

        return addr.getStreet1().contains(VACANT_INDICATOR);
    }

    private List<AddressComponent> findMissingComponents(MailingAddress addr) {
        List<AddressComponent> missingComponents = new ArrayList<>();

        if (Objects.isNull(addr.getStreet1())) {
            return missingComponents;
        }

        if (addr.getStreet1().contains(INVALID_INDICATOR)) {
            String[] split = StringUtils.split(addr.getStreet1());

            for (String token : split) {
                if (token.contains(INVALID_INDICATOR)) {
                    String invalidComponent = token.substring(INVALID_INDICATOR.length() + 1);

                    try {
                        missingComponents.add(AddressComponent.valueOf(invalidComponent.toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        // ignore
                    }
                }
            }
        }

        return missingComponents;
    }

}
