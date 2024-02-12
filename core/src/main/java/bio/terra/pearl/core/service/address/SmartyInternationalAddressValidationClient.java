package bio.terra.pearl.core.service.address;

import bio.terra.pearl.core.model.address.AddressComponent;
import bio.terra.pearl.core.model.address.AddressValidationResultDto;
import bio.terra.pearl.core.model.address.MailingAddress;
import com.smartystreets.api.international_street.Candidate;
import com.smartystreets.api.international_street.Components;
import com.smartystreets.api.international_street.Lookup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Validation client for addresses in the United States. Ignores `country` field, so be sure
 * that you are giving it an address in the United States.
 */
@Component
@Slf4j
public class SmartyInternationalAddressValidationClient implements AddressValidationClient {

    private SmartyClient client;

    SmartyInternationalAddressValidationClient(SmartyClient client) {
        this.client = client;
    }

    @Override
    public AddressValidationResultDto validate(MailingAddress address) throws AddressValidationException {

        Lookup lookup = mailingAddressToLookup(address);

        try {
            client.send(lookup);
        } catch (Exception e) {
            throw new AddressValidationException(e.getMessage(), HttpStatusCode.valueOf(400));
        }

        if (lookup.getResult().length == 0) {
            return AddressValidationResultDto.builder().valid(false).build();
        }

        return candidateToValidationResult(lookup.getResult()[0]);
    }

    private Lookup mailingAddressToLookup(MailingAddress addr) {
        Lookup lookup = new Lookup();

        lookup.setAddress1(addr.getStreet1());
        lookup.setAddress2(addr.getStreet2());
        lookup.setAdministrativeArea(addr.getState());
        lookup.setLocality(addr.getCity());
        lookup.setPostalCode(addr.getPostalCode());
        lookup.setCountry(addr.getCountry());

        return lookup;
    }

    private AddressValidationResultDto candidateToValidationResult(Candidate candidate) {

        if (isValid(candidate)) {
            return AddressValidationResultDto
                    .builder()
                    .valid(true)
                    .suggestedAddress(suggestedAddress(candidate))
                    .hasInferredComponents(requiredInference(candidate))
                    .build();
        } else {
            return AddressValidationResultDto
                    .builder()
                    .valid(false)
                    .invalidComponents(invalidComponents(candidate))
                    .build();
        }
    }

    private boolean isValid(Candidate candidate) {
        if (Objects.isNull(candidate.getAnalysis().getVerificationStatus())
            || Objects.isNull(candidate.getAnalysis().getAddressPrecision())) {
            return false;
        }
        // verifies that the analysis was verified at a delivery point level or at the
        // maximum possible for the country (some countries do not have data down to
        // the delivery point, so can only verify to a certain level of precision).
        // Ambiguous doesn't always mean undeliverable; e.g., an ambiguous scenario
        // is when two addresses exist, one with company name and one without.
        return (
            candidate.getAnalysis().getVerificationStatus().contains("Verified")
            || candidate.getAnalysis().getVerificationStatus().contains("Ambiguous")
        )
        &&
        (
            candidate.getAnalysis().getAddressPrecision().contains("DeliveryPoint")
            || candidate.getAnalysis().getAddressPrecision().equals(candidate.getAnalysis().getMaxAddressPrecision()
        ));
    }

    private MailingAddress suggestedAddress(Candidate candidate) {
        Components components = candidate.getComponents();
        return MailingAddress
                .builder()
                .city(components.getLocality())
                .state(components.getAdministrativeArea())
                .street1(candidate.getAddress1())
                .street2(candidate.getAddress2())
                .country("US") // this API can only return addresses in the US
                .postalCode(components.getPostalCode())
                .createdAt(null)
                .lastUpdatedAt(null)
                .build();
    }

    private List<AddressComponent> invalidComponents(Candidate candidate) {
        return findComponentsWithValues(
                    candidate.getAnalysis().getChanges().getComponents(),
                    INVALID
                );
    }

    private boolean requiredInference(Candidate candidate) {
        return !findComponentsWithValues(
                    candidate.getAnalysis().getChanges().getComponents(),
                    REQUIRED_INFERENCE
                ).isEmpty();
    }

    private List<AddressComponent> findComponentsWithValues(Components components, List<String> changes) {
        List<AddressComponent> out = new ArrayList<>();
        if (changes.contains(components.getPremiseNumber())
                || changes.contains(components.getPremise())) {
            out.add(AddressComponent.HOUSE_NUMBER);
        }

        if (changes.contains(components.getThoroughfareName())) {
            out.add(AddressComponent.STREET_NAME);
        }

        if (changes.contains(components.getThoroughfareTrailingType())) {
            out.add(AddressComponent.STREET_TYPE);
        }

        if (changes.contains(components.getPostalCode())) {
            out.add(AddressComponent.POSTAL_CODE);
        }

        if (changes.contains(components.getAdministrativeArea())) {
            out.add(AddressComponent.STATE_PROVINCE);
        }

        if (changes.contains(components.getSubBuilding())) {
            out.add(AddressComponent.SUBPREMISE);
        }

        return out;
    }

    private static final List<String> INVALID = List.of(
            "Unrecognized");

    private static final List<String> REQUIRED_INFERENCE = List.of(
            "Added", "Identified-AliasChange");
}
