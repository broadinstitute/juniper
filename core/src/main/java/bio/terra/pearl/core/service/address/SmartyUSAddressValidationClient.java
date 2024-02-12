package bio.terra.pearl.core.service.address;

import bio.terra.pearl.core.model.address.AddressComponent;
import bio.terra.pearl.core.model.address.AddressValidationResultDto;
import bio.terra.pearl.core.model.address.MailingAddress;
import com.smartystreets.api.us_street.Candidate;
import com.smartystreets.api.us_street.Components;
import com.smartystreets.api.us_street.Lookup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Validation client for addresses in the United States. Ignores `country` field, so be sure
 * that you are giving it an address in the United States.
 */
@Component
@Slf4j
public class SmartyUSAddressValidationClient implements AddressValidationClient {

    private SmartyClient client;

    SmartyUSAddressValidationClient(SmartyClient client) {
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

        List<Candidate> results = lookup.getResult();

        if (results.isEmpty()) {
            return AddressValidationResultDto.builder().valid(false).build();
        }

        return candidateToValidationResult(results.get(0));
    }

    private Lookup mailingAddressToLookup(MailingAddress addr) {
        Lookup lookup = new Lookup();

        lookup.setStreet(addr.getStreet1());
        lookup.setStreet2(addr.getStreet2());
        lookup.setState(addr.getState());
        lookup.setCity(addr.getCity());
        lookup.setZipCode(addr.getPostalCode());

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
        if (Objects.isNull(candidate.getAnalysis().getDpvMatchCode())) {
            return false;
        }
        return candidate.getAnalysis().getDpvMatchCode().contains("Y");
    }

    private MailingAddress suggestedAddress(Candidate candidate) {
        Components components = candidate.getComponents();
        return MailingAddress
                .builder()
                .city(components.getCityName())
                .state(components.getState())
                .street1(candidate.getDeliveryLine1())
                .country("US") // this API can only return addresses in the US
                .postalCode(components.getZipCode() + "-" + components.getPlus4Code())
                .createdAt(null)
                .lastUpdatedAt(null)
                .build();
    }

    private List<AddressComponent> invalidComponents(Candidate candidate) {
        Set<AddressComponent> components = new HashSet<>();

        if (Objects.nonNull(candidate.getAnalysis().getDpvFootnotes()))
            DPV_FOOTNOTES_INVALID_ADDRESS_COMPONENTS.forEach((key, val) -> {
                if (candidate.getAnalysis().getDpvFootnotes().contains(key)) {
                    components.addAll(val);
                }
            });

        if (Objects.nonNull(candidate.getAnalysis().getFootnotes()))
            GENERAL_FOOTNOTES_INVALID_ADDRESS_COMPONENTS.forEach((key, val) -> {
                if (candidate.getAnalysis().getFootnotes().contains(key)) {
                    components.addAll(val);
                }
            });

        return components.stream().toList();
    }

    private boolean requiredInference(Candidate candidate) {
        AtomicBoolean requiredInference = new AtomicBoolean(false);

        if (Objects.nonNull(candidate.getAnalysis().getFootnotes()))
            GENERAL_FOOTNOTES_INFERRED_ADDRESS_COMPONENTS.forEach((key, val) -> {
                if (candidate.getAnalysis().getFootnotes().contains(key)) {
                    requiredInference.set(true);
                }
            });

        return requiredInference.get();
    }

    // The following maps are somewhat arcane, but are used by all USPS-compliant
    // APIs. If we needed to change APIs in the future, they will likely return
    // the same information, and it would be mostly a matter of replacing the
    // call itself.

    // If these strings are present in the dpv footnotes, it means that there are invalid
    // address components
    // See: https://www.smarty.com/docs/cloud/us-street-api#dpvfootnotes
    private static final Map<String, List<AddressComponent>> DPV_FOOTNOTES_INVALID_ADDRESS_COMPONENTS = Map.of(
            "C1", List.of(AddressComponent.SUBPREMISE),
            "CC", List.of(AddressComponent.SUBPREMISE),
            "N1", List.of(AddressComponent.SUBPREMISE),
            "M1", List.of(AddressComponent.HOUSE_NUMBER),
            "M3", List.of(AddressComponent.HOUSE_NUMBER)
    );

    // If these strings are present in the general footnotes, it means that there are invalid
    // address components
    // See: https://www.smarty.com/docs/cloud/us-street-api#footnotes
    private static final Map<String, List<AddressComponent>> GENERAL_FOOTNOTES_INVALID_ADDRESS_COMPONENTS = Map.of(
            "C#", List.of(AddressComponent.CITY, AddressComponent.STATE_PROVINCE, AddressComponent.POSTAL_CODE),
            "F#", List.of(AddressComponent.STREET_NAME, AddressComponent.HOUSE_NUMBER, AddressComponent.STREET_TYPE),
            "H#", List.of(AddressComponent.SUBPREMISE),
            "S#", List.of(AddressComponent.SUBPREMISE),
            "V#", List.of(AddressComponent.CITY, AddressComponent.STATE_PROVINCE)
    );


    // If any of these strings are in the general footnotes, then there are inferred components
    // that were needed to map an address. The inferred components are most likely what were
    // inferred, but the API is ambiguous, so they should not be relied upon strictly
    private static final Map<String, List<AddressComponent>> GENERAL_FOOTNOTES_INFERRED_ADDRESS_COMPONENTS = Map.of(
            "A#", List.of(AddressComponent.POSTAL_CODE),
            "B#", List.of(AddressComponent.CITY, AddressComponent.STATE_PROVINCE),
            "K#", List.of(AddressComponent.STREET_NAME),
            "L#", List.of(AddressComponent.STREET_NAME, AddressComponent.STREET_TYPE),
            "M#", List.of(AddressComponent.STREET_NAME)
            // U#, P# and N# both could be included here, but I believe they shouldn't.
            // They all indicate _standardization_ of the address, so address
            // components changed, but they did not require inference - the address
            // is valid either way.
    );

}
