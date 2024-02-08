package bio.terra.pearl.core.service.address;

import bio.terra.pearl.core.model.address.AddressValidationResultDto;
import bio.terra.pearl.core.model.address.MailingAddress;

import java.util.List;

public interface AddressValidationClient {

    AddressValidationResultDto validate(MailingAddress address) throws AddressValidationException;

    List<MailingAddress> autocomplete(String street1);
}
