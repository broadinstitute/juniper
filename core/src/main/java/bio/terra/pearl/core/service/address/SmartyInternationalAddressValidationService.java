package bio.terra.pearl.core.service.address;

import bio.terra.pearl.core.model.address.AddressComponent;
import bio.terra.pearl.core.model.address.AddressValidationResultDto;
import bio.terra.pearl.core.model.address.MailingAddress;
import com.smartystreets.api.international_street.Candidate;
import com.smartystreets.api.international_street.Components;
import com.smartystreets.api.international_street.Lookup;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Validation client for international addresses. Due to the complexity of global
 * address formats, does not support all countries, but only a verified subset of
 * countries.
 * <br>
 * Many of these countries likely have edge cases that break our validation. We
 * should make sure that users always have the ability to override if necessary.
 */
@Component
@Slf4j
public class SmartyInternationalAddressValidationService implements AddressValidationService {

    private SmartyClient client;

    SmartyInternationalAddressValidationService(SmartyClient client) {
        this.client = client;
    }

    // see: https://www.iban.com/country-codes
    // if a country is given that is not one of these countries, then
    // validation will always error with an IllegalArgumentException
    public static final List<String> SUPPORTED_COUNTRIES = List.of(
            "CA",
            "GB",
            "MX",
            "AU",
            "TR",
            "ES",
            "PL",
            "DE",
            "FR",
            "IT",
            "CZ",
            "BR",
            "SE",
            "CH"
    );


    private static final List<String> APARTMENT_ON_OWN_LINE = List.of("GB", "FR", "IT", "CH");
    @Override
    public AddressValidationResultDto validate(MailingAddress address) throws AddressValidationException {

        if (SUPPORTED_COUNTRIES.stream().noneMatch(val -> val.equalsIgnoreCase(address.getCountry()))) {
            throw new IllegalArgumentException("Cannot validate country " + address.getCountry());
        }

        Lookup lookup = mailingAddressToLookup(address);

        client.send(lookup);
        
        if (Objects.isNull(lookup.getResult()) || lookup.getResult().length == 0) {
            return AddressValidationResultDto.builder().valid(false).build();
        }

        return candidateToValidationResult(address, lookup.getResult()[0]);
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

    private AddressValidationResultDto candidateToValidationResult(MailingAddress input, Candidate candidate) {

        if (isValid(candidate)) {
            return AddressValidationResultDto
                    .builder()
                    .valid(true)
                    .suggestedAddress(suggestedAddress(input, candidate))
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

        // often when a post box is provided (e.g. in the UK) it seems that it always
        // has a precision of Locality. Unfortunately, I think this means that it cannot
        // go all the way to a delivery point level of precision as they do not have
        // international PO box data for every country like they do in the US. So,
        // we have to just trust that it works.
        if (!StringUtils.isEmpty(candidate.getComponents().getPostBox())
                && candidate.getAnalysis().getAddressPrecision().equals("Locality")) {
            return true;
        }

        // Ambiguous doesn't always mean undeliverable; e.g., an ambiguous scenario
        // could be when two addresses exist, one with company name and one without.
        // Both are shippable.
        return (
            candidate.getAnalysis().getVerificationStatus().contains("Verified")
            || candidate.getAnalysis().getVerificationStatus().contains("Ambiguous")
        ) && candidate.getAnalysis().getAddressPrecision().contains("DeliveryPoint");
    }

    private MailingAddress suggestedAddress(MailingAddress input, Candidate candidate) {

        // many countries put the apartment on its own line
        if (APARTMENT_ON_OWN_LINE.contains(input.getCountry())) {
            return suggestedAddressSubpremiseOnItsOwnLine(input, candidate);
        } else if (input.getCountry().equals("TR")) {
            return suggestedAddressDependentLocalityOnItsOwnLine(input, candidate);
        }

        return standardSuggestedAddress(input, candidate);
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
        if ((Objects.nonNull(components.getPremiseNumber()) && changes.contains(components.getPremiseNumber()))
                || (Objects.nonNull(components.getPremise()) && changes.contains(components.getPremise()))) {
            out.add(AddressComponent.HOUSE_NUMBER);
        }

        if (Objects.nonNull(components.getThoroughfareName()) && changes.contains(components.getThoroughfareName())) {
            out.add(AddressComponent.STREET_NAME);
        }

        if (Objects.nonNull(components.getThoroughfareTrailingType()) && changes.contains(components.getThoroughfareTrailingType())) {
            out.add(AddressComponent.STREET_TYPE);
        }

        if (Objects.nonNull(components.getPostalCode()) && changes.contains(components.getPostalCode())) {
            out.add(AddressComponent.POSTAL_CODE);
        }

        if (Objects.nonNull(components.getLocality()) && changes.contains(components.getLocality())) {
            out.add(AddressComponent.CITY);
        }

        if (Objects.nonNull(components.getAdministrativeArea()) && changes.contains(components.getAdministrativeArea())) {
            out.add(AddressComponent.STATE_PROVINCE);
        }

        if (Objects.nonNull(components.getSubBuilding()) && changes.contains(components.getSubBuilding())) {
            out.add(AddressComponent.SUBPREMISE);
        }

        return out;
    }

    private static final List<String> INVALID = List.of(
            "Unrecognized");

    private static final List<String> REQUIRED_INFERENCE = List.of(
            "Added", "Identified-AliasChange");

    // --------- Suggested Address Translation Methods ---------
    private MailingAddress suggestedAddressSubpremiseOnItsOwnLine(MailingAddress input, Candidate candidate) {
        Components components = candidate.getComponents();
        // if there is a subpremise in some countries (e.g., GB), then the first street line is
        // the subpremise, so we need both street1 and street2. In most countries,
        // the second address line from smarty contains city/state/postal info, so
        // we don't want that in a freeform line
        if (!StringUtils.isEmpty(components.getSubBuilding())) {
            return MailingAddress
                    .builder()
                    .city(components.getLocality())
                    .state(components.getAdministrativeArea())
                    .street2(candidate.getAddress2())
                    .street1(candidate.getAddress1())
                    .country(input.getCountry())
                    .postalCode(components.getPostalCode())
                    .createdAt(null)
                    .lastUpdatedAt(null)
                    .build();
        }

        return standardSuggestedAddress(input, candidate);
    }

    private MailingAddress suggestedAddressDependentLocalityOnItsOwnLine(MailingAddress input, Candidate candidate) {
        Components components = candidate.getComponents();
        // if there is a subpremise in some countries (e.g., GB), then the first street line is
        // the subpremise, so we need both street1 and street2. In most countries,
        // the second address line from smarty contains city/state/postal info, so
        // we don't want that in a freeform line
        if (!StringUtils.isEmpty(components.getDependentLocality())) {
            return MailingAddress
                    .builder()
                    .city(components.getLocality())
                    .state(components.getAdministrativeArea())
                    .street2(candidate.getAddress2())
                    .street1(candidate.getAddress1())
                    .country(input.getCountry())
                    .postalCode(components.getPostalCode())
                    .createdAt(null)
                    .lastUpdatedAt(null)
                    .build();
        }

        return standardSuggestedAddress(input, candidate);
    }

    private MailingAddress standardSuggestedAddress(MailingAddress input, Candidate candidate) {
        Components components = candidate.getComponents();

        return MailingAddress
                .builder()
                .city(components.getLocality())
                .state(components.getAdministrativeArea())
                .street2("")
                .street1(candidate.getAddress1())
                .country(input.getCountry())
                .postalCode(components.getPostalCode())
                .createdAt(null)
                .lastUpdatedAt(null)
                .build();
    }
}
