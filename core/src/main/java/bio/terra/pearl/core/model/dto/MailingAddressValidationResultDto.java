package bio.terra.pearl.core.model.dto;

import bio.terra.pearl.core.model.participant.MailingAddress;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Slf4j
public class MailingAddressValidationResultDto {
    // This property indicates strong failure -
    // the address was incomplete or invalid
    // and would not work
    private boolean validationFailed;

    // This property provides a suggestion for
    // an improved mailing address; typically,
    // this means the address was valid, but
    // could be improved
    private MailingAddress suggestedAddress;

    // Components which were missing from
    // an invalid address
    private List<AddressComponentType> missingComponents;

    // Raw string values of things that were not able to
    // resolve, e.g. "12345678" in "12345678 Main St"
    private List<String> unresolvedTokens;

    // When validated the same address over and over again,
    // use the same sessionId
    private UUID sessionId;

    // see: https://developers.google.com/maps/documentation/places/web-service/supported_types#table2
    // This is only a subset of the relevant fields for us.
    enum AddressComponentType {
        COUNTRY("country"),
        STREET_ADDRESS("street_address"),
        STREET_NUMBER("street_number"),
        LOCALITY("locality"), // e.g. Boston
        POST_BOX("post_box"),
        FLOOR("floor"),
        POSTAL_CODE("postal_code"),
        POSTAL_CODE_PREFIX("postal_code_prefix"),
        POSTAL_CODE_SUFFIX("postal_code_suffix"),
        POSTAL_TOWN("postal_town"),
        PLUS_CODE("plus_code"),
        ROOM("room"),
        ROUTE("route"),
        SUBLOCALITY("sublocality"),
        NEIGHBORHOOD("neighborhood"),
        OTHER("other");


        private String value;
        AddressComponentType(String val) {
            this.value = val;
        }

        @Override
        public String toString() {
            return value;
        }
    }
}
