package bio.terra.pearl.core.model.address;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class AddressValidationResultDto {
    // If true, indicates that there is a valid address which
    // could be found.
    // (makes no claim about the validity of the provided address)
    private boolean valid;

    // This property indicates a valid mailing address provided
    // by USPS/international databases
    private MailingAddress suggestedAddress;

    // Components which were missing from
    // an invalid address
    private List<AddressComponent> invalidComponents;

    // Indicates if the suggested address required
    // inferring components of the address that
    // were either missing or incorrect
    private Boolean hasInferredComponents;

    // USPS considers this place vacant, which might
    // lead to misdeliveries
    private Boolean vacant;
}
