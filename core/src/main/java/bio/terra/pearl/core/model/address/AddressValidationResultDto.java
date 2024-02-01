package bio.terra.pearl.core.model.address;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class AddressValidationResultDto {
    // If false, property indicates strong failure -
    // the address was incomplete or invalid and an
    // improved address could not be found.
    private boolean valid;

    // This property provides a suggestion for
    // an improved mailing address; typically,
    // this means the address was valid, but
    // could be improved
    private MailingAddress suggestedAddress;

    // Components which were missing from
    // an invalid address
    // see: https://developers.google.com/maps/documentation/places/web-service/supported_types#table2
    //      for possible values
    private List<String> missingComponents;

    // Raw string values of things that were not able to
    // resolve, e.g. "12345678" in "12345678 Main St"
    private List<String> unresolvedTokens;

    // When validating the same address over and over again,
    // use the same sessionId
    private UUID sessionId;
}
